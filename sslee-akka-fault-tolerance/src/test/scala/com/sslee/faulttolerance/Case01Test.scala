package com.sslee.faulttolerance

import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem

class Case01Test extends TestKit(ActorSystem("testsystem")) 
  with WordSpecLike with StopSystemAfterAll{
  
  import com.sslee.faulttolerance.case01.ActorCase01
  import com.sslee.faulttolerance.case01.MessageCase01._
  
  "Actor Case01 sinario" must {
    
    /*
    "case 01 child01 sending Message" in {
      val actor = system.actorOf(ActorCase01.props("ParentActor"))
      actor ! Message("start message")
    }
    */
    
    /*
    "case 02 child02 sending stop" in {
      val actor = system.actorOf(ActorCase01.props("ParentActor"))
      actor ! StopMessage
      
      println("###############################")
      Thread.sleep(1000l)
      actor ! Message("second Message")
    }
    */
    
    "case 03 child02 sending interrupt" in {
      val actor = system.actorOf(ActorCase01.props("ParentActor"))
      actor ! InterruptMessage
      //actor ! StopMessage
      actor ! Message("second Message")
      
      Thread.sleep(20000l)
    }
  }
  
}