package com.sslee.pool.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object RemotePoolBackendBoot extends App {
  
  val system = ActorSystem("backend", ConfigFactory.load("remote-pool-backend"))
  
}