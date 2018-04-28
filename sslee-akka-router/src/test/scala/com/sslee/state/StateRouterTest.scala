package com.sslee.state

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import akka.testkit.ImplicitSender
import com.sslee.StopSystemAfterAll
import org.scalatest.WordSpecLike
import com.sslee.state.routemessages._
import com.sslee.state._
import akka.actor.Props
import akka.testkit.TestProbe
import scala.concurrent.duration._

class StateRouterTest extends TestKit(ActorSystem("StateRouterSystem")) 
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "StateRouter" must {
    "state router seperate Task work and Router" in {
      
      val rightProbe = TestProbe()
      val leftProbe = TestProbe()
      
      val router = system.actorOf(Props(new StateRouteActor(rightProbe.ref, leftProbe.ref)))
      
      router ! MyMessage("is LeftMessage")
      leftProbe expectMsg "is LeftMessage"
      rightProbe.expectNoMessage(2 seconds)
      
      router ! RouteStateRight
      router ! MyMessage("is RightMessage")
      rightProbe expectMsg "is RightMessage"
      rightProbe.expectNoMessage(2 seconds)
      
    }
  }
  
}