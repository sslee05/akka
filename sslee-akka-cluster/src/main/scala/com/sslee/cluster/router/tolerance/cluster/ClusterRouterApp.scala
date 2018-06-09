package com.sslee.cluster.router.tolerance.cluster

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import akka.cluster.Cluster

object ClusterRouterApp extends App {
  
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
          port = 2552
        }
      }
      
      cluster {
        seed-nodes = [
        "akka.tcp://ClusterSystem@127.0.0.1:2551"
        ]
        roles = ["master"]
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
  val roles = system.settings.config.getStringList("akka.cluster.roles")
  
  if(roles.contains("master")) {
    Cluster(system).registerOnMemberUp {
      val client = system.actorOf(Props(new ClusterClient()),"client")
      client ! "hellow"
      Thread.sleep(2000L)
      client ! "errorMsg"
      Thread.sleep(2000L)
      client ! "afterMsg"
    }
  }
  
}