package com.sslee.pool.local

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike

import com.sslee.StopSystemAfterAll

import akka.routing.Broadcast
import akka.routing.FromConfig
import akka.testkit.TestKit
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.routing.DefaultResizer
import scala.concurrent.duration._
import akka.actor.Props

class MaxMinTest extends TestKit(ActorSystem("resizeActorSystme",ConfigFactory.load("local-pool"))) 
  with WordSpecLike with MustMatchers { //with StopSystemAfterAll {
  
  import com.sslee.pool.routemessges._
  
  "rezier test " must {
    "upper-bound is " in {
      
      val router = system.actorOf(FromConfig.props(Props[ResizeRouteeActor]),"localRouter")
      
      println(s"######## send Broadcate #############")
      router ! Broadcast(MyMessage("first message"))
      
      //expectMsg(MyReplyMessage("first message"))
      
      println(s"######## send 1 overload message #############")
      //router ! MyMessage("overload message first")
      //router ! MyMessage("overload message first2")
      //router ! MyMessage("overload message first3")
      //router ! MyMessage("overload message first4")
      //router ! MyMessage("overload message first5")
      (0 to 10) foreach { i =>
        router ! Broadcast(MyMessage("overload message $i"))
      }
      
      receiveWhile() {
        case msg => println("BBBBBBBBBBBBBBBBB")
      }
      
      //Thread.sleep(10000L)
    }
    
    "test" in {
      val resizer = DefaultResizer(
        lowerBound = 2,
        upperBound = 10,
        rampupRate = 0.2)

      resizer.rampup(pressure = 9, capacity = 10) must be(0) 
      resizer.rampup(pressure = 5, capacity = 5) must be(1)
      resizer.rampup(pressure = 6, capacity = 6) must be(2)
    }
  }
  
}