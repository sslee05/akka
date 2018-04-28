package com.sslee.remote

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

object BackendRemoteDeployMain extends App with RequestTimeout {
  
  val config = ConfigFactory.load("backend")
  val system = ActorSystem("backend",config)
}