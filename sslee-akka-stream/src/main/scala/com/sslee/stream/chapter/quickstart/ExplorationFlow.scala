package com.sslee.stream.chapter.quickstart

import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Source,Sink,Flow,RunnableGraph,Keep}
import akka.NotUsed
import akka.Done
import scala.concurrent.Future

object ExplorationFlow extends App {
  
  implicit val system = ActorSystem("FlowSystem")
  implicit val es = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  //Source, Flow, Sink 선언 
  val tt: Range = 1 to 10
  val source: Source[Int,NotUsed] = Source(1 to 10)
  val flow : Flow[Int,Int,NotUsed] = Flow[Int].map(i => i * 2)
  val sink: Sink[Int,Future[Done]] = Sink.foreach(println)
  
  //Source + Flow = Source  => Source + Sink
  val sourceJoinFlow: Source[Int,NotUsed] = source.via(Flow[Int].map(i => i * 2))
  val graph: RunnableGraph[Future[Done]] = sourceJoinFlow.toMat(sink)(Keep.right)
  val result:Future[Done] = graph.run()
  
  //Flow + Sink = Sink => Source + Sink
  val sinkJoinFlow: Sink[Int,Future[Done]] = flow.toMat(sink)(Keep.right)
  val graph02: RunnableGraph[Future[Done]] = source.toMat(sinkJoinFlow)(Keep.right)
  val result2: Future[Done] = graph02.run()
  Future.sequence(List(result,result2)).onComplete(_ => system.terminate())
  
}