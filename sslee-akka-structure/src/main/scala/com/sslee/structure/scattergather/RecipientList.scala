package com.sslee.structure.scattergather

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props

class RecipientList(recipients: Seq[ActorRef]) extends Actor with ActorLogging {
  
  def receive = {
    case msg: Any =>
      log.debug(s"#####$self received message $msg")
      recipients foreach(_ ! msg)
  }
}

object RecipientList {
  def props(recipients: Seq[ActorRef]) = Props(new RecipientList(recipients))
}