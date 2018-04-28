package com.sslee.testdriven.chapter01

import akka.actor.{Actor, ActorLogging}

object SideEffectingActor {
  case class Greeting(message: String)  
}

class SideEffectingActor extends Actor with ActorLogging {
  
  import SideEffectingActor._
  
  def receive = {
    case Greeting(message) => log.info("Hellow {}!",message)
  }
}