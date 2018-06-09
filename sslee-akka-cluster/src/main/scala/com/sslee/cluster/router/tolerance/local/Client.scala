package com.sslee.cluster.router.tolerance.local

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.routing.BalancingPool
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.Terminated

class Client extends Actor with ActorLogging {
  
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
    
  //val router = context.actorOf(BalancingPool(1).props(Props(new Worker())),"workerRouter")
  //val router = context.actorOf(FromConfig.props(Props(new Worker())),"workerRouter")
  val router = context.actorOf(Props(new Worker()),"workerRouter")
  
  def receive = {
    
    case Terminated(routee) =>
      log.debug(s"$this #####Client-receive:Terminated $routee")
    case msg =>
      log.debug(s"$this #####Client-receive:$msg")
      router ! msg
  }
  
}