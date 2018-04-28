package com.sslee.remote

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.util.Timeout

object BackendNodeMain extends App with RequestTimeout {
  
  val config = ConfigFactory.load("backend")
  val system = ActorSystem("backend",config)
  
  implicit val timeout: Timeout = configuredRequestTimeout(config)
  
  system.actorOf(BoxOffice.props, BoxOffice.name)
  
}