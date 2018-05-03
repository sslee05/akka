package com.sslee.channel.publishsubscribe

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike

import com.sslee.channel.StopSystemAfterAll

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.actor.DeadLetter
import akka.actor.Props
//import com.sslee.channel.deadletter.EchoActor
import com.sslee.channel.messages._
import scala.concurrent.duration._
import akka.actor.PoisonPill
import akka.testkit.ImplicitSender
import akka.testkit.TestActors.EchoActor


class DeadLetterTest extends TestKit(ActorSystem("DeadLetterSystem"))
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "DeadLetterSystem" must {
    "deadLetter Monitor" in {
      
      val deadLetterMonitor = TestProbe()
      
      system.eventStream.subscribe(deadLetterMonitor.ref,classOf[DeadLetter])
      
      val echoActor = system.actorOf(Props(new EchoActor),"echoActor")
      echoActor ! Order(2)
      deadLetterMonitor expectNoMessage (3 seconds)
      
      echoActor ! PoisonPill
      echoActor ! Order(4)
      
      val dead = deadLetterMonitor.expectMsgType[DeadLetter]
      println(s"msg ${dead.message}")
      println(s"sender ${dead.sender}")
      println(s"recipient ${dead.recipient}")
      
      dead.message must be(Order(4))
      dead.sender must be(testActor)
      dead.recipient must be(echoActor)
    }
  }
  
}