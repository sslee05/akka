package com.sslee.cluster.router.tolerance.local

import akka.actor.ActorLogging
import akka.actor.Actor

class Worker extends Actor with ActorLogging {
  
  val errorMsg = "errorMsg"
  
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
  
  
  def receive = {
    case `errorMsg` =>
      log.debug(s"$this #####Worker-receive: occurError $errorMsg")
      throw new IllegalArgumentException("occur RuntimeException!")
    case msg => 
      log.debug(s"$this #####Worker-receive: normal message $msg")
  }
  
   
  
}