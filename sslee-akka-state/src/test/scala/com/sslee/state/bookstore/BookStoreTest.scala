package com.sslee.state.bookstore

import org.scalatest.MustMatchers
import com.sslee.state.StopSystemAfterAll
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import com.sslee.state.bookstore._
import com.sslee.state.bookstore.messages._
import akka.actor.Props
import akka.testkit.TestProbe
import akka.actor.FSM.SubscribeTransitionCallBack
import akka.actor.FSM.CurrentState
import akka.actor.FSM.Transition

class BookStoreTest extends TestKit(ActorSystem("BookStoreSystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "BookStoreApp" must {
    "init state " in {
      
      val publisher = system.actorOf(Props(new Publisher(2,1)))
      val testProbe = TestProbe()
      
      val bookStore = system.actorOf(Props(new BookStore(publisher)))
      bookStore ! SubscribeTransitionCallBack(testProbe.ref)
      
      testProbe.expectMsg(CurrentState(bookStore,WaitRequest))
    }
    
    "RequestBook state" in {
      val publisher = system.actorOf(Props(new Publisher(2,2)))
      val monitorProbe = TestProbe()
      val requestProbe = TestProbe()
      
      val bookStore = system.actorOf(Props(new BookStore(publisher)))
      bookStore ! SubscribeTransitionCallBack(monitorProbe.ref)
      monitorProbe expectMsg(CurrentState(bookStore, WaitRequest))
      
      bookStore ! RequestBook(1, requestProbe.ref)
      
      monitorProbe expectMsg (new Transition(bookStore, WaitRequest, WaitPublisher))
      monitorProbe expectMsg (new Transition(bookStore, WaitPublisher, ProcessRequest))
      monitorProbe expectMsg (new Transition(bookStore, ProcessRequest, WaitRequest))
      requestProbe expectMsg BookReply(1,Right(1))
      
      println("################# second request ###########")
      bookStore ! RequestBook(2, requestProbe.ref)
      
      monitorProbe expectMsg (new Transition(bookStore, WaitRequest, ProcessRequest))
      monitorProbe expectMsg (new Transition(bookStore, ProcessRequest, WaitRequest))
      requestProbe expectMsg BookReply(2, Right(2))
      
      println("################# thrid request expect Sold Out ###########")
      bookStore ! RequestBook(3, requestProbe.ref)
      
      monitorProbe expectMsg (new Transition(bookStore, WaitRequest, WaitPublisher))
      monitorProbe expectMsg (new Transition(bookStore, WaitPublisher, SoldOut))
      monitorProbe expectMsg (new Transition(bookStore, SoldOut, WaitRequest))
      requestProbe expectMsg BookReply(3, Left("Sold Out"))      
      
      
    }
  }
  
}