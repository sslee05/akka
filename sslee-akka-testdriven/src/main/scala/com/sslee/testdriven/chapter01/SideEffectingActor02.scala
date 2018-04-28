package com.sslee.testdriven.chapter01

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

object SideEffectingActor02 {
  
  def props(listener: Option[ActorRef] = None) = 
    Props(new SideEffectingActor02(listener))
    
  case class Greeting(message: String) 
}

class SideEffectingActor02(listener: Option[ActorRef]) extends Actor with ActorLogging {
 
  import SideEffectingActor02._
  
  def receive = {
    case Greeting(who) => 
      val message= "Hellow " + who + "!"
      log.info(message)
      listener.foreach(_ ! message)
  }
}