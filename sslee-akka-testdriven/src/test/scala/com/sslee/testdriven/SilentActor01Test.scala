package com.sslee.testdriven

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import aia.testdriven.StopSystemAfterAll
import org.scalatest.WordSpecLike
import com.sslee.testdriven.chapter01.SilentActor
import akka.testkit.TestActorRef
import akka.actor.Props

class SilentActor01Test extends TestKit(ActorSystem("testsystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "A Slient Actor" must {
    
    import com.sslee.testdriven.chapter01.SilentActor._
    
    "change state when it receives a message, single threaded" in {
      
      //단일 thread test를 위한 TestActorRef를 만듬
      val silentActor = TestActorRef[SilentActor]
      silentActor ! SilentMessage("whisper")
      //기저 actor의 internalState에 접근 이는 implicit로 AnyMustWrapper로  
      silentActor.underlyingActor.internalState must contain("whisper")
    }
    
    "change state when it receives a message, multi-threaed" in {
      //TestSystem를 이용하여 Actor  생성 
      val silentActor = system.actorOf(Props[SilentActor],"s3")
      silentActor ! SilentMessage("whisper1")
      silentActor ! SilentMessage("whisper2")
      
      //TestKit에는 testActor를 제공한다.
      silentActor ! GetState(testActor)
      //TestKit의 method 인 expectMsg를 통해 testActor에게 보내진 메시지를 검사한다.
      expectMsg(Vector("whisper1", "whisper2"))
    }
  }
  
}