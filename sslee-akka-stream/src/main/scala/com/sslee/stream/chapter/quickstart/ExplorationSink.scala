package com.sslee.stream.chapter.quickstart

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.NotUsed
import akka.util.ByteString
import akka.stream.scaladsl.FileIO
import java.nio.file.Paths
import scala.concurrent.Future
import akka.stream.IOResult
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Keep
import akka.Done

object ExplorationSink extends App {
  
  implicit val system = ActorSystem("streamSystem")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  
  //어디로 보낼지는 모르지만 file 읽어 file내용을 ByteString으로 
  val source: Source[ByteString, Future[IOResult]] = 
    FileIO.fromPath(Paths.get("/Users/sslee/temp/factorial.txt"))
    
  //어떤 거에 연결 될지 모르지만 ByteString을 받아 File 로  
  val sink: Sink[ByteString, Future[IOResult]] = 
    FileIO.toPath(Paths.get("/Users/sslee/temp/factorial2.txt"))
    
  //source + sink = 1개의 열린 출력 + 1개의 열린 입력 = Graph(여기서의 최종 설계도)
  val graph: RunnableGraph[Future[IOResult]] = source to sink
  
  //설계도를 실행 실행결과 로는 Future에 Source에서의 IOResult가 들어있고 이를 println으로 console출력 한다.
  graph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    //system.terminate()
  }
  
  val graphKeepRight: RunnableGraph[Future[IOResult]] = source.toMat(sink)(Keep.right)
  graphKeepRight.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    //system.terminate()
  }
  
  val graphKeepBoth: RunnableGraph[(Future[IOResult], Future[IOResult])] = source.toMat(sink)(Keep.both)
  val tt: (Future[IOResult], Future[IOResult])  = graphKeepBoth.run()
  Future.sequence(List(tt._1, tt._2)).foreach { rs => rs.foreach(println(_)) }
  
}