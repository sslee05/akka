package com.sslee.testdriven

import org.scalatest.MustMatchers
import aia.testdriven.StopSystemAfterAll
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import com.sslee.testdriven.chapter01.SideEffectingActor02
import akka.actor.UnhandledMessage

class SideEffectingActor02Test extends TestKit(ActorSystem("testsystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  import com.sslee.testdriven.chapter01.SideEffectingActor02._
  
  "SideEffectingActor2 Actor" must {
    
    "say Hellow World! when a Greeting(Wold) is sent to it" in {
      val props = SideEffectingActor02.props(Some(testActor))
      val actor = system.actorOf(props,"greeter02-1")
      
      actor ! Greeting("World")
      expectMsg("Hellow World!")
    }
    
    "say something else and see what happens" in {
      val props = SideEffectingActor02.props(Some(testActor))
      val actor = system.actorOf(props,"greeter02-02")
      
      system.eventStream.subscribe(testActor, classOf[UnhandledMessage])
      actor ! "World"
      expectMsg(UnhandledMessage("World", system.deadLetters, actor))
    }
  }
  
}