package com.sslee.group.local



import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.PoisonPill
import akka.routing.Broadcast

object LocalGroupBoot extends App {
  
  val system = ActorSystem("localGroupSystem",ConfigFactory.load("local-group"))
  
  val createActor = system.actorOf(Props(new RouteeCreateActor(2)),"createActor")
  val router = system.actorOf(FromConfig.props,"localRouter")
  
  router ! Broadcast(PoisonPill)
  Thread.sleep(1000L)
  
  import com.sslee.group.routemessages._
  
  router ! Broadcast(MyMessage("hellow routee!"))
}