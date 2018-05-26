package com.sslee.stream.buffer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.NotUsed
import akka.stream.Attributes
import scala.concurrent.duration._
import akka.stream.FanInShape
import akka.stream.FanInShape2
import akka.stream.scaladsl._
import akka.stream.ClosedShape

object BufferTest01App extends App {
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  // the buffer size of this map is 1
  val section: Flow[Int,Int,NotUsed] = 
    Flow[Int].map{i => println(s"A:$i"); (i * 2)}.async.addAttributes(Attributes.inputBuffer(initial = 1, max = 1))
  
  // the buffer size of this map is the default
  val flow  =  section.via(Flow[Int].map{i => println(s"B:$i"); i / 2}.async.addAttributes(Attributes.inputBuffer(initial = 1, max = 1)))
  //val flow  =  section.via(Flow[Int].map{i => println(s"B:$i"); i / 2}.async)
  
  case class Tick(i: Int)
  
  RunnableGraph.fromGraph(
     GraphDSL.create(){ implicit b =>
       import GraphDSL.Implicits._
       
       val zipper: FanInShape2[Tick,Int,Int] = 
         b.add(ZipWith[Tick,Int,Int]{(tick, count) => println(s"C:${(tick,count)}");count}.async)
       
         //3초마다  Tick()를 방출 시킴 
         Source.tick(initialDelay = 3.second, interval = 3.second, Tick(0)) ~> zipper.in0
         
         Source.tick(initialDelay = 1.second, interval = 1.second, "message!")
            .conflateWithSeed(seed = (_) ⇒ 1){(count, x) ⇒ println(s"#####start$count:$x"); count + 1} ~> zipper.in1

        zipper.out ~> Sink.foreach(println)
        ClosedShape
     }
  ).run()
  
  
      
}