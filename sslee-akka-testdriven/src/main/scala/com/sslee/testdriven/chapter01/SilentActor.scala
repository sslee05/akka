package com.sslee.testdriven.chapter01

import akka.actor.Actor
import akka.actor.ActorRef

object SilentActor {
  case class SilentMessage(data: String)
  case class GetState(receiver: ActorRef)
}

class SilentActor extends Actor {
  
  import SilentActor._
  
  var internalState = Vector[String]()
  
  def receive = {
    case SilentMessage(message) => internalState = internalState :+ message
    case GetState(receiver) => receiver ! internalState
  }
}