package com.sslee.group

import org.scalatest.MustMatchers
import com.sslee.StopSystemAfterAll
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import akka.actor.Props
import com.sslee.group.local.RouteeCreateActor
import akka.routing.RoundRobinGroup
import akka.routing.Broadcast
import akka.testkit.ImplicitSender
import akka.actor.PoisonPill
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig

class GroupTest extends TestKit(ActorSystem("GroupRouterSystem",ConfigFactory.load("local-group"))) 
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "Group local router" must {
    "router with creater " in {
      
      import com.sslee.group._
      import routemessages._
      
      val createActor = system.actorOf(Props(new RouteeCreateActor(3)),"createActor")
      
      /*
      val paths = List(
        "/user/createActor/myRoutee-0",
        "/user/createActor/myRoutee-1"
      )
      
      val router = system.actorOf(RoundRobinGroup(paths).props(),"groupRouter")
      */
      
      val router = system.actorOf(FromConfig.props,"localRouter")
      
      router ! Broadcast(PoisonPill)
      Thread.sleep(1000L)
      router ! MyMessage("hellow routee!")
      
      expectMsg(MyReplyMessage("hellow routee!"))
      
      router ! Broadcast(PoisonPill)
      Thread.sleep(1000L)
      router ! Broadcast(MyMessage("hellow routee!"))
          
      val rs = receiveWhile() { case MyReplyMessage(msg) => msg }
      rs.size must be(2)
      
    }
  }
  
}