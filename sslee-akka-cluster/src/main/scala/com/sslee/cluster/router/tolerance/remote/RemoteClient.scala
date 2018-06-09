package com.sslee.cluster.router.tolerance.remote

import akka.actor.Props
import akka.actor.Terminated
import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.cluster.router.tolerance.local.Worker
import akka.actor.Address
import akka.actor.AddressFromURIString
import akka.remote.routing.RemoteRouterConfig
import akka.routing.FromConfig
import akka.routing.BalancingPool

class RemoteClient extends Actor with ActorLogging {
  
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
    
  val address = Seq(    //akka.tcp://backend@0.0.0.0:2551
    AddressFromURIString("akka.tcp://RemoteSystemBackend@0.0.0.0:2551")
  )
  
  //val router = context.actorOf(RemoteRouterConfig(BalancingPool(1),address).props(Props(new Worker())),"workerRouter")
  val router = context.actorOf(RemoteRouterConfig(FromConfig(),address).props(Props(new Worker())),"workerRouter")
  
  def receive = {
    
    case Terminated(routee) =>
      log.debug(s"$this #####Client-receive:Terminated $routee")
    case msg =>
      log.debug(s"$this #####Client-receive:$msg")
      router ! msg
  }
  
}