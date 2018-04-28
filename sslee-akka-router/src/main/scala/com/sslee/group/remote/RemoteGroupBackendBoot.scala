package com.sslee.group.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props

object RemoteGroupBackendBoot extends App {
  
  import com.sslee.group.local._
  import com.sslee.group.routemessages._
  
  val system = ActorSystem("backend",ConfigFactory.load("remote-group-backend"))
  val createActor = system.actorOf(Props(new RouteeCreateActor(2)),"createActor")
  
  
}