package com.sslee.stream.fan

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.{Graph,FlowShape}
import akka.NotUsed
import akka.stream.scaladsl._
import akka.stream.scaladsl.{Broadcast, GraphDSL,RunnableGraph}
import com.sslee.stream.resources._
import akka.util.ByteString
import spray.json._
import com.sslee.stream.EventMarshalling
import java.nio.file.StandardOpenOption
import java.nio.file.StandardOpenOption._

import akka.stream.{ ActorAttributes, ActorMaterializer, IOResult }
import akka.stream.scaladsl.{ FileIO, BidiFlow, Flow, Framing, Keep, Sink, Source }

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import spray.json._
import akka.stream.UniformFanOutShape
import java.nio.file.Paths
import java.nio.file.Path
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure

object BroadcastGraphStage extends App with EventMarshalling {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  //Shape는 Graph의 입출력 갯수를 정의 한다.
  type FlowLike = Graph[FlowShape[Event, ByteString], NotUsed]
  
  def processState(logId: String): FlowLike = {
    val jsFlow: Flow[Event, ByteString, NotUsed] = Flow[Event].map(e => ByteString(e.toJson.compactPrint))
    
    //fromGraph(FlowShape)
    Flow.fromGraph(
      GraphDSL.create(){ implicit builder => //GraphDSL.builder
        import GraphDSL.Implicits._
        
        val bcast: UniformFanOutShape[Event, Event] = builder.add(Broadcast[Event](5)) // Broadcase 열린 출력 5개
        val js: FlowShape[Event, ByteString] = builder.add(jsFlow)
        
        val ok = Flow[Event].filter(e => e.state == Ok)
        val warning = Flow[Event].filter(e => e.state == Warning)
        val error = Flow[Event].filter(e => e.state == Error)
        val critical = Flow[Event].filter(e => e.state == Critical)
        
        bcast ~> js.in
        bcast ~> ok ~> jsFlow ~> logFileSink(logId,Ok)
        bcast ~> warning ~> jsFlow ~> logFileSink(logId, Warning)
        bcast ~> error ~> jsFlow ~> logFileSink(logId, Error)
        bcast ~> critical ~> jsFlow ~> logFileSink(logId, Critical)
        
        //broadcast의 Inlet과 Flow.out 으로부터 Outlet를 조합한 FlowShape를 반환 한다.
        val result:FlowShape[Event, ByteString] = FlowShape(bcast.in, js.out)
        result
      }
    )
  }
  
  val src = 
    FileIO.fromPath(Paths.get("/Users/sslee/temp/stream-example01.json"))
      .via(JsonFraming.objectScanner(1024 * 1024))
      .map(b => b.decodeString("UTF-8"))
      .map(s => s.parseJson.convertTo[Event])
  
  def logFileSink(logId: String,state: State): Sink[ByteString, Future[IOResult]] = 
    FileIO.toPath(logFile(logId,state), Set(CREATE,WRITE,APPEND))
  
  def logFile(logId: String,state: State): Path = 
    Paths.get(s"/Users/sslee/temp/$logId-${State.norm(state)}")
    
  
  def logFileSink: Sink[ByteString, Future[IOResult]] = 
    FileIO.toPath(Paths.get("/Users/sslee/work/temp/bidiFlowJson.log"))
    
  val resultFn:Future[IOResult] = src.via(processState("10")).toMat(logFileSink)(Keep.right).run()
  resultFn.onComplete {
    case Success(count) =>
      println(count)
      system.terminate()
    case Failure(e) => 
      println(e)
      system.terminate()
  }
}