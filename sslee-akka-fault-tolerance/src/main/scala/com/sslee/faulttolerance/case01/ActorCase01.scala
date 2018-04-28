package com.sslee.faulttolerance.case01

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorLogging
import java.util.UUID
import akka.actor.PoisonPill
import akka.actor.Terminated

object ActorCase01 {
  def props(name: String) = Props(new ActorCase01(name))
}

class ActorCase01(name: String) extends Actor with ActorLogging {
  
  log.info(s"ActorCase01 actor name is ${name} called constructor ${this}")
  
  val child01 = context.actorOf(ActorCase01Child01.props(UUID.randomUUID.toString))
  
  /*
  val actors = (1 to 2).map { i =>
    val child = context.actorOf(ActorCase01Child01.props(UUID.randomUUID.toString))
    context.watch(child)
  }
  *
  */
  
  import MessageCase01._
  def receive = {
    case StopMessage => 
      log.info(s"I am ${this} of ActorCase01 receive message StopMessage")
      child01 ! StopMessage
      //actors.head ! StopMessage
      //actors(1) ! Message("sending first child stopMessage")
    case InterruptMessage =>
      log.info(s"I am ${this} of ActorCase01 receive message InterruptMessage")
      child01 ! InterruptMessage
      //actors.head ! InterruptMessage
      //actors(1) ! Message("sending first child InterruptMessage")
    case m @ Message(msg) => 
      log.info(s"I am ${this} of ActorCase01 receive message Message(${msg})")
      child01 ! m
      //actors.foreach(a => a ! m)
  }
  
  override def preStart() = {
    log.info(s"#####ActorCase01 actor name is ${name} preStart called")
  }
    
  override def preRestart(reason: Throwable, msg: Option[Any]) = {
    log.info(s"#####ActorCase01 actor name is ${name} preRestart called ${msg} ${reason}")
    super.preRestart(reason, msg)
  }
  
  override def postStop() = { 
    log.info(s"#####ActorCase01 actor name is ${name} postStop called")
  }
    
  override def postRestart(reason: Throwable) = { 
    log.info(s"#####ActorCase01 actor name is ${name} postRestart called ${reason}")
    super.postRestart(reason)
  }
}