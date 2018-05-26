package com.sslee.stream.chapter.flow


import com.sslee.stream.EventMarshalling
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import akka.NotUsed
import akka.util.ByteString
import spray.json._
import com.sslee.stream.resources._
import scala.concurrent.Future
import akka.stream.IOResult
import java.nio.file.Paths
import scala.util.Failure
import scala.util.Success

object BidiFlowApp extends App with EventMarshalling {
  
  implicit val system = ActorSystem("BidiFlowExample")
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  val path = Files.createTempFile("log","json")
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
      """
  
  val jsonByte = jsonStr.getBytes("UTF-8")
  Files.write(path, jsonByte, StandardOpenOption.APPEND)
  
  val inFlow: Flow[ByteString, Event, NotUsed] = JsonFraming.objectScanner(1024 * 1024)
      .map(b => b.decodeString("UTF-8"))
      .map(s => s.parseJson.convertTo[Event])
      
  val outFlow: Flow[Event, ByteString, NotUsed] = 
    Flow[Event].map(e => ByteString(e.toJson.compactPrint))
  
  
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(path) 
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("/Users/sslee/work/temp/bidiFlowJson.log"))
  
  val bidiFlow: BidiFlow[ByteString, Event, Event, ByteString, NotUsed] = 
    BidiFlow.fromFlows(inFlow, outFlow)
    
  val flow: Flow[ByteString, ByteString, NotUsed] = 
    bidiFlow.join(Flow[Event].filter(e => e.state == Error ))
  
  val runGraph: RunnableGraph[Future[IOResult]] = source.via(flow).toMat(sink)(Keep.right)
  
  runGraph.run().onComplete{
    case Success(s) => println(s"#####$s")
    case Failure(e) => println(s"#####$e")
  }
    
}