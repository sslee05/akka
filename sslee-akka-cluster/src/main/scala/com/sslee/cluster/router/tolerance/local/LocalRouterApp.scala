package com.sslee.cluster.router.tolerance.local

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props

object LocalRouterApp extends App {
  
  val config = ConfigFactory.parseString("""
    akka {
      loggers = ["akka.event.slf4j.Slf4jLogger"]
      loglevel = "DEBUG"
      logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
      
      actor.deployment {
        /client/workerRouter {
          router = round-robin-pool
          nr-of-instances = 1
        }
      }
    }
    """)
  
  val system = ActorSystem("LocalSystem",config)
  val client = system.actorOf(Props[Client],"client")
  client ! "hellow"
  Thread.sleep(2000L)
  client ! "errorMsg"
  Thread.sleep(2000L)
  client ! "afterMsg"
}