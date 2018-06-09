package com.sslee.cluster.router.tolerance.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import com.sslee.cluster.router.tolerance.local.Client

object RemoteRouterBackend extends App {
  val config = ConfigFactory.parseString("""
    akka {
      loglevel = DEBUG
      stdout-loglevel = DEBUG
      loggers = ["akka.event.slf4j.Slf4jLogger"]
      #loggers = ["akka.event.Logging$DefaultLogger"]
    
      actor {
        provider = "akka.remote.RemoteActorRefProvider"
      }
    
      remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
          hostname = "127.0.0.1"
          port = 2551
        }
      }
    }
    """)
  
  val system = ActorSystem("RemoteSystemBackend",config)
  //val client = system.actorOf(Props[Client])
}