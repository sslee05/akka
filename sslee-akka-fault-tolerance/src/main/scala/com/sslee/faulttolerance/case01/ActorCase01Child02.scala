package com.sslee.faulttolerance.case01

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.actor.PoisonPill

object ActorCase01Child02 {
  
  def props(name: String) = Props(new ActorCase01Child02(name)) 
}

class ActorCase01Child02(name: String) extends Actor with ActorLogging {
  
  log.info(s"ActorCase01Child02 actor name is ${name} called constructor ${this}")
  
  import MessageCase01._
  
  def receive = {
    
    case Message(msg) => 
      log.info(s"I am ${this} of ActorCase01Child02 receive message Message(${msg})")
    case StopMessage => 
      log.info(s"I am ${this} of ActorCase01Child02 receive message StopMessage")
      self ! PoisonPill
    case InterruptMessage => 
      log.info(s"I am ${this} of ActorCase01Child02 receive message InterruptMessage")
      throw new ForceRestartException
  }
  
  override def preStart() = {
    log.info(s"#####ActorCase01Child02 actor name is ${name} preStart called")
  }
    
  override def preRestart(reason: Throwable, msg: Option[Any]) = {
    log.info(s"#####ActorCase01Child02 actor name is ${name} preRestart called ${msg} ${reason}")
    super.preRestart(reason, msg)
  }
  
  override def postStop() = { 
    log.info(s"#####ActorCase01Child02 actor name is ${name} postStop called")
  }
    
  override def postRestart(reason: Throwable) = { 
    log.info(s"#####ActorCase01Child02 actor name is ${name} postRestart called ${reason}")
    super.postRestart(reason)
  }
  
}