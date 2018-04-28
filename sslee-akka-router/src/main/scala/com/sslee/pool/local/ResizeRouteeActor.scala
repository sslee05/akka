package com.sslee.pool.local

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props

class ResizeRouteeActor extends Actor with ActorLogging {
  
  import com.sslee.pool.routemessges._
  
  def receive = {
    case MyMessage(value) =>
      log.debug(s"##### $self $value")
      Thread.sleep(3000L)
      sender ! MyReplyMessage(value)
  }
}


