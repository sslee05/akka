package com.sslee.conf

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object PingAndPongBoot extends App {
  
  val config = ConfigFactory.load("logtest")
  val system = ActorSystem("pingPongSystme",config)
  //val system = ActorSystem("pingPongSystme")
  
  val pingActor = system.actorOf(PingActor.props)
  
  pingActor ! "start"
  pingActor ! "finish"
  
}