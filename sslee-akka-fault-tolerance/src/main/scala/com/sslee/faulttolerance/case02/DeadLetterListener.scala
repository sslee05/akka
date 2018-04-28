package com.sslee.faulttolerance.case02

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.DeadLetter

class DeadLetterListener extends Actor with ActorLogging {
  
  def receive = {
    case DeadLetter(msg, from, to) => 
      log.info(s"####deadLetter message ${msg} from ${from} to ${to}")
  }
}