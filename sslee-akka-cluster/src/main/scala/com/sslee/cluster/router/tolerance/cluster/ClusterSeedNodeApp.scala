package com.sslee.cluster.router.tolerance.cluster

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ClusterSeedNodeApp extends App {
  val config = ConfigFactory.parseString("""
    akka {
      loglevel = DEBUG
      stdout-loglevel = DEBUG
      loggers = ["akka.event.slf4j.Slf4jLogger"]
      #loggers = ["akka.event.Logging$DefaultLogger"]
    
      actor {
        provider = "akka.cluster.ClusterActorRefProvider"
      }
    
      remote {
        enabled-transports = ["akka.remote.netty.tcp"]
        netty.tcp {
          hostname = "127.0.0.1"
          port = 2551
        }
      }
      
      cluster {
        seed-nodes = [
        "akka.tcp://ClusterSystem@127.0.0.1:2551"
        ]
        roles = ["seed"]
        auto-down = on
    
        role {
          seed.min-nr-of-members = 1
          master.min-nr-of-members = 1
          worker.min-nr-of-members = 1
        }
      }
    }
    """)
  
  val system = ActorSystem("ClusterSystem",config)
}