package com.sslee.cluster.wordcount

import akka.stream.scaladsl.Source
import akka.util.Timeout
import akka.NotUsed
import akka.stream.scaladsl.Sink
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import akka.Done
import scala.util.Success
import scala.util.Failure

object TestApp extends App {
  import akka.pattern.ask
  implicit val askTimeout = Timeout(5.seconds)
  val words: Source[String, NotUsed] =
    Source(List("hello", "hi"))
    
  implicit val system = ActorSystem("test")
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  val actor = system.actorOf(Props[TestActor])
  
  val result = words.mapAsync(parallelism = 5)(elem => (actor ? elem).mapTo[String])
    // continue processing of the replies from the actor
    .map{replyMessageFromActor => 
       println(s"#### relay message from actor $replyMessageFromActor") 
     }
    .runWith(Sink.ignore)
    
  result.onComplete{
    case Success(a) => println(s"####a=>$a")
    case Failure(b) => println(s"####b=>$b")
  }
}

class TestActor extends Actor with ActorLogging {
  def receive = {
    case msg => 
      println(s"##### msg $msg $self")
      //throw new RuntimeException("testexception")
      sender() ! msg 
  }
}