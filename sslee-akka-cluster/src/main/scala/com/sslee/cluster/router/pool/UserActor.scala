package com.sslee.cluster.router.pool

import akka.actor.ActorLogging
import akka.actor.Actor

class UserActor extends Actor with ActorLogging {
  
  def receive = {
    case msg => 
      log.debug(s"######reply mesasge $msg")
  }
  
}