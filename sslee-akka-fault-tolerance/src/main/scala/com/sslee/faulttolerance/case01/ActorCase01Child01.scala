package com.sslee.faulttolerance.case01

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.PoisonPill
import java.util.UUID
import akka.actor.Terminated

object ActorCase01Child01 {
  
  def props(name: String) = Props(new ActorCase01Child01(name)) 
}


class ActorCase01Child01(name: String) extends Actor with ActorLogging {
  
  log.info(s"ActorCase01Child01 actor name is ${name} called constructor ${this}")
  
  import MessageCase01._
  
  val childActor = context.actorOf(ActorCase01Child02.props(UUID.randomUUID.toString))
  
  context.watch(childActor)
  
  def receive = {
    case m @ Message(msg) => 
      log.info(s"I am ${this} of ActorCase01Child01 receive message Message(${msg})")
      childActor ! m 
    case StopMessage => 
      log.info(s"I am ${this} of ActorCase01Child01 receive message StopMessage")
      //childActor ! StopMessage
      self ! PoisonPill
    case InterruptMessage => 
      log.info(s"I am ${this} of ActorCase01Child01 receive message InterruptMessage")
      //childActor ! InterruptMessage
      throw new ForceRestartException
    case Terminated(actorRef) =>  
      log.info(s"I am ${this} of ActorCase01Child01 Terminated actorRef is ${actorRef}")
  }
  
  override def preStart() = {
    log.info(s"#####ActorCase01Child01 actor name is ${name} preStart called")
  }
    
  override def preRestart(reason: Throwable, msg: Option[Any]) = {
    log.info(s"#####ActorCase01Child01 actor name is ${name} preRestart called ${msg} ${reason}")
    //self ! Message("transfer message")
    super.preRestart(reason, msg)
  }
  
  override def postStop() = { 
    log.info(s"#####ActorCase01Child01 actor name is ${name} postStop called")
  }
    
  override def postRestart(reason: Throwable) = { 
    log.info(s"#####ActorCase01Child01 actor name is ${name} postRestart called ${reason}")
    super.postRestart(reason)
  }
}