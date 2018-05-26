package com.sslee.stream.fan

import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import com.sslee.stream.EventMarshalling
import com.sslee.stream.resources._
import scala.concurrent.Future
import akka.stream.IOResult
import java.nio.file.Paths
import akka.NotUsed
import akka.util.ByteString
import spray.json._
import akka.stream.SourceShape

import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.FlowShape
import akka.stream.UniformFanInShape

trait MergeGraphStage { this: EventMarshalling =>
  
  //implicit val system = ActorSystem("MergeGraphExample")
  //implicit val ec = system.dispatcher
  //implicit val mat = ActorMaterializer()
  
  def mergeNotOk(logId: String): Source[ByteString, NotUsed] = {
    
    val warning = logFileSource(logId, Warning)
    val error = logFileSource(logId, Error)
    val critical = logFileSource(logId, Critical)
    
    Source.fromGraph(
      GraphDSL.create(){ implicit builder =>
        import GraphDSL.Implicits._
        
        val warningShape: SourceShape[ByteString] = builder.add(warning)
        val errorShape: SourceShape[ByteString] = builder.add(error)
        val criticalShape: SourceShape[ByteString] = builder.add(critical)
        
        //UniformFanInShape[ByteString, ByteString]
        val merge: UniformFanInShape[ByteString, ByteString] = builder.add(Merge[ByteString](3))
        
        warningShape ~> merge
        errorShape ~> merge
        criticalShape ~> merge
        
        //Source.fromGraph 이므로 SourceShape를 이용하여 Merge의 outlet으로 출력포트 1개를 가지는
        //SourceShape를 만든다.
        SourceShape(merge.out)
      }    
    )
  }
  
  def logFileSource(logId: String, state: State): Source[ByteString, Future[IOResult]] = 
    FileIO.fromPath(Paths.get("/Users/sslee/temp", s"$logId-${State.norm(state)}"))
      .via(JsonFraming.objectScanner(1024 * 1024))
  
}