package com.sslee.group.local

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig
import com.sslee.group.routemessages._
import akka.actor.Props
import akka.routing.Broadcast

object LocalGroupResizeBoot extends App {
  
  val system = ActorSystem("localGroupResizeSystem",ConfigFactory.load("local-group"))
  
  val router = system.actorOf(FromConfig.props,"localRouter")
  val resizer = system.actorOf(Props(new ResizeCreateActor(2,Props(new MyRouteeActor()),router)),"createActor")
  
  //resizer가 생성할 시간을 준다.
  Thread.sleep(1000L)
  
  router ! MyMessage("Hellow routees")
  
  resizer ! Resizing(5)
  Thread.sleep(1000L) // resizer가 routee를 생성할 시간을 준다.
  
  router ! Broadcast(MyMessage("Hellow routees"))
  
}