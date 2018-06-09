package com.sslee.cluster.router

import akka.actor.Props
import akka.actor.ActorLogging
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.routing.FromConfig
import scala.concurrent.duration._

object SimpleRouterApp {
  /**
   * Start with:
   *   sbt run-main sample.cluster.simple.SimpleRouterApp 2551
   *   sbt run-main sample.cluster.simple.SimpleRouterApp 2552
   *   sbt run-main sample.cluster.simple.SimpleRouterApp 2553
   */
  def main(args: Array[String]): Unit = {

    // Override the configuration of the port
    // when specified as program argument
    if (args.nonEmpty) System.setProperty("akka.remote.netty.tcp.port", args(0))

    val conf = ConfigFactory.load(ConfigFactory.parseString("""
      akka.actor {
        provider = "akka.cluster.ClusterActorRefProvider"
      }
      akka.remote {
        log-remote-lifecycle-events = off
        netty.tcp {
          hostname = "127.0.0.1"
          port = 0
        }
      }
      akka.cluster {
        seed-nodes = [
          "akka.tcp://ClusterSystem@127.0.0.1:2551",
          "akka.tcp://ClusterSystem@127.0.0.1:2552"]
        auto-down = on
      }
      akka.log-dead-letters=1000
      akka.actor.deployment {
        /producer/router {
            router = broadcast-pool
            nr-of-instances = 100
            cluster {
              enabled = on
              routees-path = "/user/routee"
              allow-local-routees = off
            }
          }
      }"""))

    val system = ActorSystem("ClusterSystem", conf)
    Cluster(system)
    system.actorOf(Props[Routee], name = "routee")
    system.actorOf(Props[Producer], name = "producer")

  }
}

class Routee extends Actor with ActorLogging {
  def receive = {
    case msg ⇒
      println("############Routee received: {} from {}", msg, sender.path)
  }
}

class Producer extends Actor with ActorLogging {
  val router = context.actorOf(Props.empty.withRouter(FromConfig),
    name = "router")
  import context.dispatcher
  val tickTask = context.system.scheduler.schedule(3.seconds, 3.seconds, self, "tick")
  var count = 0

  override def postStop(): Unit = {
    tickTask.cancel()
  }

  def receive = {
    case "tick" ⇒
      println("############ TICK")
      count += 1
      router ! count
  }
}