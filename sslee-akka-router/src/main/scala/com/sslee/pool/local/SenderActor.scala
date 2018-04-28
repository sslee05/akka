package com.sslee.pool.local

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.pool.routemessges._
import akka.actor.ActorRef

class SenderActor(router: ActorRef) extends Actor with ActorLogging {
  
  def receive = {
    case MyMessage(msg) => 
      router ! MyMessage(msg)
    case MyReplyMessage(msg) =>
      log.debug(s"############## $msg reply message from routee")
  }
}
