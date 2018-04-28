package com.sslee.faulttolerance

import akka.actor.ActorLogging
import akka.actor.Actor

object LifeCycleActor {
  case object ForceRestart
  case object SampleMessage
  private class ForceRestartException extends IllegalStateException("force restart")
}

class LifeCycleActor extends Actor with ActorLogging { //self =>
  
  import LifeCycleActor._
  
  println(s"######### called Constructor ${this}")
  
  override def preStart(): Unit = 
    println(s"######### called preStart ${this}")
    
  override def postStop(): Unit = 
    println(s"######### called postStop ${this}")
    
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println(s"######### called preRestart ${this}")
    super.preRestart(reason, message)
  }
  
  override def postRestart(reason: Throwable): Unit = {
    println(s"######### called postRestart ${this}")
    super.postRestart(reason)
  }
  
  def receive = {
    case ForceRestart =>
      println(s"######### receive message restart ${this}")
      throw new ForceRestartException
    case msg: AnyRef =>
      println(s"######### receive message ${msg} ${this}")
      sender() ! msg
  }
  
}