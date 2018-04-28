package com.sslee.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.remote.RemoteActorRefProvider

object TestLoad extends App {
  val config = ConfigFactory.load("backend")
  implicit val system = ActorSystem("Frontend", config)
  //implicit val system = ActorSystem("frontendTest", config)
  
}