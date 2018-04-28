package com.sslee.faulttolerance

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.actor.Props

class LifeCycleActorTest extends TestKit(ActorSystem("testSystem")) 
 with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  import com.sslee.faulttolerance.LifeCycleActor._
  
//  override def afterAll(): Unit = {
//    system.terminate()
//  }
  
  "LifeCycleActor must" must {
    "life cycle called" in {
      
      val testActorRef = system.actorOf(Props[LifeCycleActor],"LifeCycleActor")
      watch(testActorRef)
      testActorRef ! ForceRestart
      testActorRef.tell(SampleMessage,testActor)
      expectMsg(SampleMessage)
      //system.stop(testActorRef)
      //Thread.sleep(2000l)
      
      system.stop(testActorRef)
      expectTerminated(testActorRef)
    }
  }
  
}