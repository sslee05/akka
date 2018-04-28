package com.sslee.conf

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.event.LoggingReceive

class PongActor extends Actor with ActorLogging {
  
  import PingActor._
  def receive = LoggingReceive {
    
    case `flag` =>
      log.debug(s"###USER-MSG: receive msg finish this Actor stopping...")
      context.stop(self) 
    case msg: String => 
      log.debug(s"###USER-MSG: receive msg $msg")
  }
}

object PongActor {
  def props = Props(new PongActor)
}