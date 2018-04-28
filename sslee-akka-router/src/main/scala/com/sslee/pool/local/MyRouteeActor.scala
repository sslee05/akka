package com.sslee.pool.local

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.routing.Broadcast
import akka.actor.Props

import com.sslee.pool.routemessges._

class MyRouteeActor extends Actor with ActorLogging {
  
  def receive = {
    case MyMessage(value) =>
      log.debug(s"############## $self $value")
      sender() ! MyReplyMessage(value)
  }
}
