package com.sslee.cluster.router.pool

import akka.actor.ActorLogging
import akka.actor.Actor

class StateWorker02 extends Actor with ActorLogging {
  
  def receive = {
    case StateMessage(msg, replyTo) => 
      log.debug(s"#####Worker-message:StateMessage($msg,$replyTo")
      replyTo ! s"$msg to reply $msg"
  }
  
}