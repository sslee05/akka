package com.sslee.stream.buffer

import akka.stream.scaladsl._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.SinkShape
import akka.stream.OverflowStrategy
import scala.concurrent.duration._
import java.nio.file.Paths
import java.nio.file.StandardOpenOption._
import java.nio.file.StandardOpenOption
import akka.stream.IOResult
import akka.util.ByteString
import akka.stream.Attributes
import scala.concurrent.duration._


object BufferTest02 extends App {
  
  implicit val system = ActorSystem()
  implicit val ec =  system.dispatcher
  implicit val mat = ActorMaterializer()
  
  /*
  val slowSink = Sink.foreach[Int] { i =>
    Thread.sleep(3000L)
    println(s"slow Sink $i")
  }.addAttributes(Attributes.inputBuffer(initial = 1, max = 1))
  */
  
  val slowSink = Sink.foreach[Seq[Int]] { i =>
    Thread.sleep(3000L)
    println(s"slow Sink $i")
  }
  
  val buffer = Flow[Int].buffer(10, OverflowStrategy.dropHead)
    .groupedWithin(3, Duration(300, SECONDS))
    .map(i => i.toVector).async
  //val buffer = Flow[Int].buffer(10, OverflowStrategy.dropHead).async.addAttributes(Attributes.inputBuffer(initial = 1, max = 1))
  
  val fastSink = Sink.foreach[Int](i => println(s"fast Sinke $i"))
  
  val sink = Sink.fromGraph(
    GraphDSL.create(){ implicit builder =>
      
      import GraphDSL.Implicits._
      
      val bcast = builder.add(Broadcast[Int](2))
      
      bcast ~> buffer ~> slowSink
      //bcast ~> slowSink
      bcast ~> fastSink
      
      SinkShape(bcast.in)
    }    
  )
  
  
  val result = Source(1 to 5).to(sink).run()
  
  
}