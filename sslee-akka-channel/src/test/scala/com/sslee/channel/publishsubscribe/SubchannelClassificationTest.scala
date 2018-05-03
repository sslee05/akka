package com.sslee.channel.publishsubscribe

import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import com.sslee.channel.StopSystemAfterAll
import akka.testkit.TestKit
import akka.testkit.TestProbe
import com.sslee.channel.publishsubscribe._
import com.sslee.channel.messages._
import scala.concurrent.duration._

class SubchannelClassificationTest extends TestKit(ActorSystem("SubchannelSystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "SubchannelClassification " must {
    "used String startsWiths" in {
      
      val resultActor = TestProbe()
      
      val bus = new MsgBusViaSubchannelClassification()
      bus.subscribe(resultActor.ref, "abc")
      
      bus publish News("abc", "hellow")
      resultActor expectMsg "hellow"
      
      bus publish News("bc", "hellow")
      resultActor expectNoMessage (3 seconds)
      
      bus publish News("abcdefg","hellow")
      resultActor expectMsg "hellow"
      
    }
  }
  
}