package com.sslee.state

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import com.sslee.state._
import akka.actor.Props
import com.sslee.state.messages._
import akka.testkit.TestProbe
import akka.actor.FSM.SubscribeTransitionCallBack
import akka.actor.FSM.CurrentState
import akka.actor.FSM.Transition

class InventoryStateTest extends TestKit(ActorSystem("InventoryStateSystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "Finite State Machine " must {
    "start current state" in {
      val publisher = system.actorOf(Props(new Publisher(2,2)))
  
      val inventory = system.actorOf(Props(new Inventory(publisher)))
      val stateProbe = TestProbe()
      inventory ! new SubscribeTransitionCallBack(stateProbe.ref)
      stateProbe.expectMsg(new CurrentState(inventory,WaitForRequests))
      
      val replyProbe = TestProbe()
      inventory ! new BookRequest("context01", replyProbe.ref)
      //inventory ! new BookRequest("context02", replyProbe.ref)
      
      //stateProbe.expectMsg(new Transition(inventory,WaitForRequests,WaitForPublisher))
      //stateProbe.expectMsg(new Transition(inventory,WaitForPublisher, ProcessRequest))
      //stateProbe.expectMsg(new Transition(inventory,ProcessRequest, WaitForRequests))
      //replyProbe.expectMsg(new BookReply("context01", Right(1)))
      
      println("##################")
      Thread.sleep(15000L)
      system.stop(inventory)
      
      /*
      inventory ! new BookRequest("context02", replyProbe.ref)
      stateProbe.expectMsg(new Transition(inventory,WaitForRequests,ProcessRequest))
      stateProbe.expectMsg(new Transition(inventory,ProcessRequest,WaitForRequests))
      replyProbe.expectMsg(new BookReply("context02",Right(2)))
      
      println("##################")
      
      inventory ! new BookRequest("context03", replyProbe.ref)
      stateProbe.expectMsg(new Transition(inventory,WaitForRequests, WaitForPublisher))
      stateProbe.expectMsg(new Transition(inventory,WaitForPublisher, ProcessSoldOut))
      replyProbe.expectMsg(new BookReply("context03", Left("SoldOut")))
      //stateProbe.expectMsg(new Transition(inventory,ProcessSoldOut, SoldOut))
      stateProbe.expectMsg(new Transition(inventory,ProcessSoldOut, SoldOut))
      
      println("##################")
      //inventory ! new BookRequest("context04", replyProbe.ref)
      //stateProbe.expectMsg(new CurrentState(inventory,SoldOut))
       */
      
    }
  }
}