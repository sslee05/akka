package com.sslee.conf

import akka.actor.Actor
import akka.actor.ActorLogging

class HellowWorld extends Actor with ActorLogging {
  
  def receive = {
    case msg: String => 
      val hellow = "Hellow %s".format(msg)
      sender() ! hellow
      log.info("Send response {}", hellow)
  }
  
}