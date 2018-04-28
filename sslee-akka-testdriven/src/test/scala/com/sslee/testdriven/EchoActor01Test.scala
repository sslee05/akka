package com.sslee.testdriven

import akka.testkit.ImplicitSender
import aia.testdriven.StopSystemAfterAll
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import akka.actor.Props
import com.sslee.testdriven.chapter01.EchoActor

//ImplicitSender 를 하므로써 sender를 TestKit의 sender로 자동으로 바꾸어 준다.
class EchoActor01Test extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike with ImplicitSender with StopSystemAfterAll {
  
  "Echo Actor" must {
    "Reply with the same message it receives without ask" in {
      val echoActor = system.actorOf(Props[EchoActor],"echo2")
      echoActor ! "some message"
      
      //Sender가 testKit의 Sender로 자동 바꾸어 짐으로써 expectMsg를 통해 결과를 확인 할 수 있다.
      expectMsg("some message")
    }
  }
  
}