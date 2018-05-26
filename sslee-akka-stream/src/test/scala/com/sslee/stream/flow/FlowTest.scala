package com.sslee.stream.flow

import java.nio.file.Paths
import java.time.ZonedDateTime

import scala.concurrent.Future

import org.scalatest.BeforeAndAfterAll
import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike

import com.sslee.stream.resources.Error
import com.sslee.stream.resources.Event
import com.sslee.stream.resources.LogParseException
import com.sslee.stream.resources.State

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.testkit.TestKit
import akka.util.ByteString
import akka.Done
import scala.concurrent.Await
import scala.concurrent.duration._
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import akka.stream.scaladsl.JsonFraming
import spray.json._
import com.sslee.stream.EventMarshalling
import scala.util.Failure
import scala.util.Success
import akka.stream.ActorAttributes
import akka.stream.Supervision
import akka.stream.ActorMaterializerSettings

class FlowTest extends TestKit(ActorSystem("FlowTest"))
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with EventMarshalling {
  
  val jsonStr = 
      """
      [
      {
        "host": "my-host-1",
        "service": "web-app",
        "state": "ok",
        "time": "2015-08-12T12:12:00.127Z",
        "description": "5 tickets sold to RHCP."
      },
      {
        "host": "my-host-2",
        "service": "web-app",
        "state": "ok",
        "time": "2015-08-12T12:12:01.127Z",
        "description": "3 tickets sold to RHCP."
      },
      {
        "host": "my-host-3",
        "service": "web-app",
        "state": "ok",
        "time": "2015-08-12T12:12:02.127Z",
        "description": "1 tickets sold to RHCP."
      },
      {
        "host": "my-host-3",
        "service": "web-app",
        "state": "error",
        "time": "2015-08-12T12:12:03.127Z",
        "description": "exception occurred..."
      }
      ]
      """

  override protected def afterAll() = {
    super.afterAll()
    system.terminate()
  }

  def parseLine(line: String): Option[Event] = {
    if (line.isEmpty()) None
    else {
      line.split("\\|") match {
        case Array(host, service, state, time, desc, tag, metric) =>
          val t = tag.trim()
          val m = metric.trim()

          Some(Event(
            host.trim(),
            service.trim(),
            state.trim() match {
              case State(s) => s
              case _        => throw new Exception(s"Unexpected line!: $line")
            },
            ZonedDateTime.parse(time.trim()),
            desc.trim(),
            if (t.isEmpty()) None else Some(t),
            if (m.isEmpty()) None else Some(m.toDouble)))

        case Array(host, service, state, time, desc) =>
          Some(Event(
            host.trim(),
            service.trim(),
            state.trim() match {
              case State(s) => s
              case _        => throw new Exception(s"Unexpected line!: $line")
            },
            ZonedDateTime.parse(time.trim()),
            desc.trim()))

        case _ => throw LogParseException(s"failed on line $line")
      }
    }
  }

  "FlowExample" must {

    "bytString(file) to Seq[Event]" in {
      
      val decider: Supervision.Decider = {
        case _ : LogParseException => Supervision.Resume
        case _  => Supervision.Stop
      }

      implicit val ec = system.dispatcher
      //Supervision.Decider 전략 
      implicit val mat = ActorMaterializer(
        ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      val source: Source[ByteString, Future[IOResult]] =
        FileIO.fromPath(Paths.get("/Users/sslee/work/temp/stream-source.txt"))

      //framing 을 이용하여 Event 단위를 구분한다.
      val frame: Flow[ByteString, String, NotUsed] =
        Framing.delimiter(ByteString("\n"), 1024 * 1024).map(b => b.decodeString("UTF-8"))

      //parser String 을 Event로 변환 한다.
      //collect method는 return type이 Flow#Repr 으로 type Repr[+T] = Flow[In, T, Mat]
      val parser: Flow[String, Event, NotUsed] =
        Flow[String].map(s => parseLine(s)).collect { case Some(e) => e }

      //Error 상태만 filtering
      val filter: Flow[Event, Event, NotUsed] = Flow[Event].filter(e => e.state == Error)

      //sink
      val sink: Sink[Event, Future[Seq[Event]]] = Sink.seq[Event]
      val runGraph: RunnableGraph[Future[Seq[Event]]] =
        source.via(frame).via(parser).via(filter).toMat(sink)(Keep.right)

      val result: Future[Seq[Event]] = runGraph.run()
      
      Await.result(result, 10 seconds) must be {
        Vector(Event("my-host-3", "web-app", Error, ZonedDateTime.parse("2015-08-12T12:12:03.127Z"), "exception occurred..." ))
      }
      
      //간단하게 정리 
      val result02: Future[Seq[Event]] = 
        source.via(Framing.delimiter(ByteString("\n"), 1024 * 1024).map(b => b.decodeString("UTF-8")))
          .via(Flow[String].map(s => parseLine(s)).collect{case Some(e) => e})
          .via(Flow[Event].filter(e => e.state == Error))
          .toMat(Sink.seq[Event])(Keep.right).run()
          
      Await.result(result02, 10 seconds) must be {
        Vector(Event("my-host-3", "web-app", Error, ZonedDateTime.parse("2015-08-12T12:12:03.127Z"), "exception occurred..." ))
      }
      
    }
    
    "Source json format to Seq[Event]" in {
      implicit val ec = system.dispatcher
      implicit val mat = ActorMaterializer()
      
      val path = Files.createTempFile("log",".json")
      
      val jsonByte = jsonStr.getBytes("UTF-8")
      Files.write(path, jsonByte, StandardOpenOption.APPEND)
      
      val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(path)
      //Framing으로 Event단위 추출을 위함
      val jsonByteSource: Source[ByteString, Future[IOResult]] = source.via(JsonFraming.objectScanner(1024 * 1024))
      //Source[ByteString,Future[IOResult]] 을 Source[String, Future[IOResult]]로 변환
      val strSource: Source[String, Future[IOResult]] = jsonByteSource.map(b => b.decodeString("UTF-8"))
      //String 를 Event로 변환
      val parser: Source[Event,Future[IOResult]] = strSource.map(s => s.parseJson.convertTo[Event])
      //Error filter
      val filter = Flow[Event].filter(e => e.state == Error)
      
      val sink: Sink[Event, Future[Seq[Event]]] = Sink.seq[Event]
      val graph: RunnableGraph[Future[Seq[Event]]] = parser.via(filter).toMat(sink)(Keep.right)
      
      val result: Future[Seq[Event]] = graph.run()
      
      Await.result(result, 3 seconds) must be {
        Vector(Event("my-host-3", "web-app", Error, ZonedDateTime.parse("2015-08-12T12:12:03.127Z"), "exception occurred..." ))
      }
    }
    
    "source json to Event to json" in {
      implicit val ec = system.dispatcher
      implicit val mat = ActorMaterializer()
      
      val path = Files.createTempFile("log2","json")
      val jsonByte = jsonStr.getBytes("UTF-8")
      Files.write(path, jsonByte, StandardOpenOption.APPEND)
      
      val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(path)
      val byteToStrSource: Source[ByteString, Future[IOResult]] = source.via(JsonFraming.objectScanner(1024 * 1024))
      val strSource: Source[String, Future[IOResult]] = byteToStrSource.map(b => b.decodeString("UTF-8"))
      
      //Supervision.Decider 를 이용한 전략 정책 
      val jsonFlow: Flow[String, Event, NotUsed] = 
        Flow[String].map(s => s.parseJson.convertTo[Event]).addAttributes(ActorAttributes.supervisionStrategy{
          case _: LogParseException => Supervision.Resume
          case _  => Supervision.Stop
        })
      val jsonSource: Source[Event,Future[IOResult]] = strSource via jsonFlow
      val runGraph: RunnableGraph[Future[Seq[Event]]] = jsonSource.toMat(Sink.seq[Event])(Keep.right)
      
      val result = runGraph.run()
      result.onComplete{case Success(s) => println(s"#########$s") case Failure(e) => println(e)}
      
      //간단 하게 
      val runGraph2: RunnableGraph[Future[Seq[Event]]] =  
        FileIO.fromPath(path) // Source[ByteString, Future[IOResult]]
          .via(JsonFraming.objectScanner(1024 * 1024)) // Source[ByteString, Future[IOResult]]
          .via(Flow[ByteString].map(b => b.decodeString("UTF-8"))) // Source[String, Future[IOResult]]
          .via(Flow[String].map(s => s.parseJson.convertTo[Event]))// Source[Event, Future[IOResult]]
          .toMat(Sink.seq[Event])(Keep.right) 
          
      val result2 = runGraph2.run()
      result2.onComplete{case Success(s) => println(s) case Failure(e) => println(e)}
      
    }
    
    "Error Handling with Element" in {
      
      implicit val ec = system.dispatcher
      implicit val mat = ActorMaterializer()
      
      val path = Files.createTempFile("jsonErrorExample", "json")
      val jsonByte = jsonStr.getBytes("UTF-8")
      Files.write(path, jsonByte,StandardOpenOption.APPEND)
      
      val source = FileIO.fromPath(path)
        .via(JsonFraming.objectScanner(1024 * 1024))
        .via(Flow[ByteString].map(b => b.decodeString("UTF-8")))
        //.via(Flow[String].map(s => ))
            
    }
  }

}