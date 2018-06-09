package com.sslee.cluster.router.tolerance.cluster

import akka.actor.Props
import akka.actor.Terminated
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.AddressFromURIString
import akka.routing.FromConfig
import akka.remote.routing.RemoteRouterConfig
import com.sslee.cluster.router.tolerance.local.Worker
import akka.routing.BalancingPool
import com.sslee.cluster.wordcountapp.pool.WordCounter
import akka.routing.RoundRobinPool
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.cluster.routing.ClusterRouterPool

class ClusterClient extends Actor with ActorLogging {
  
  println(s"######### called Constructor $this")
  
  override def postRestart(reason: Throwable): Unit = {
    println(s"######### called postRestart $this")
    super.postRestart(reason)
  }
  
  override def preStart() = {
    println(s"######### called preStart $this")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println(s"######### called preRestart $this")
    super.preRestart(reason, message)
  }
  
  override def postStop(): Unit = 
    println(s"######### called postStop $this")
    
  
  //val router = context.actorOf(RemoteRouterConfig(BalancingPool(1),address).props(Props(new Worker())),"workerRouter")
  //val router = context.actorOf(RemoteRouterConfig(FromConfig(),address).props(Props(new Worker())),"workerRouter")
  //val router = context.actorOf(BalancingPool(1).props(Props[Worker]),"workerRouter")
  val router = context.actorOf(ClusterRouterPool(
      RoundRobinPool(5),//init routee count in pool
      //BalancingPool(5),
      ClusterRouterPoolSettings(
        totalInstances = 1000,
        maxInstancesPerNode = 1,
        allowLocalRoutees = false,
        useRole = Some("worker")
      )
    ).props(Props[Worker]),"workerRouter")
  
  def receive = {
    
    case Terminated(routee) =>
      log.debug(s"$this #####Client-receive:Terminated $routee")
    case msg =>
      log.debug(s"$this #####Client-receive:$msg")
      router ! msg
  }
  
}