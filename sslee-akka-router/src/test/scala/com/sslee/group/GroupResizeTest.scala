package com.sslee.group

import akka.testkit.TestKit
import akka.actor.ActorSystem
import com.sslee.StopSystemAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import akka.testkit.ImplicitSender
import akka.routing.RoundRobinGroup
import akka.actor.Props
import com.sslee.group.local.ResizeCreateActor
import com.sslee.group.local.MyRouteeActor
import com.sslee.group.routemessages._
import akka.routing.Broadcast
import akka.routing.Routees
import akka.routing.GetRoutees
import akka.routing.ActorRefRoutee
import akka.routing.ActorSelectionRoutee

class GroupResizeTest extends TestKit(ActorSystem("GroupResizeSystem")) 
    with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "resize by group in local system" must {
    "resize test" in {
      
      //val resizeCreator = system.actorOf(props)
      
      //path는 넣지 않는다. routeeCreatActor 가 routee를 만들 것 이므로 따로 여기서 path를 넣으면 
      //routee 에 대한 actorSelection이 중복이 된다. 
      val router = system.actorOf(RoundRobinGroup(List.empty[String]).props,"groupRouter")
      val resizeCreator = system.actorOf(
        Props(new ResizeCreateActor(2,Props(new MyRouteeActor()),router)),"resizeCreateActor")
      
      // routee creator가 routee를 생성할 시간을 준다.
      Thread.sleep(1000L)
      
      router ! MyMessage("hellow routee!")
      expectMsg(MyReplyMessage("hellow routee!"))
      
      resizeCreator ! Resizing(4)
      Thread.sleep(1000L)
      router ! GetRoutees
      
      val rs = receiveWhile() {
        case routees: Routees =>
          routees.getRoutees.size()
      }
      
      rs.head must be(4)
      
      resizeCreator ! Resizing(3)
      Thread.sleep(1000L)
      router ! GetRoutees
      
      val rs2 = receiveWhile() {
        case routees: Routees =>
          routees.getRoutees.size()
      }
      
      rs2.head must be(3)
    }
  }
  
}