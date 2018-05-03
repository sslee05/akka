package com.sslee.channel.publishsubscribe

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import com.sslee.channel.StopSystemAfterAll
import org.scalatest.WordSpecLike
import com.sslee.channel.messages._
import com.sslee.channel.publishsubscribe._
import akka.testkit.ImplicitSender
import akka.testkit.TestProbe
import scala.concurrent.duration._

class ScanningClassificationTest extends TestKit(ActorSystem("ScanningSystem"))
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "ScanningClassification " must {
    "using duplicate classifier" in {
      
      val twoOrderedActor = TestProbe()
      val tenOrderedActor = TestProbe()
      
      val bus = new MsgViaScanningClassification()
      
      bus.subscribe(twoOrderedActor.ref, 2)
      bus.subscribe(tenOrderedActor.ref, 10)
      
      bus.publish(Order(2))
      twoOrderedActor expectMsg Order(2)
      tenOrderedActor expectNoMessage (3 seconds)
      
      bus.publish(Order(11))
      twoOrderedActor expectMsg Order(11)
      tenOrderedActor expectMsg Order(11)
      
      /*
      bus.subscribe(testActor, 3)
      bus.publish("xyzabc")
      bus.publish("12")
      expectMsg("12")
      
      bus.publish("a2c")
      expectMsg("a2c")
      * 
      */
      
    }
  }
  
}