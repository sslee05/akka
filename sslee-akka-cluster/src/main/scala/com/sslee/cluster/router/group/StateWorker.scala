package com.sslee.cluster.router.group

import akka.actor.ActorLogging
import akka.actor.Actor

class StateWorker extends Actor with ActorLogging {
  
  var cache = Map.empty[String, Int]
  
  def receive = {
    case word: String => 
      log.debug(s"##### StateWorker receive message $word")
      cache = cache ++ cache.get(word).map(i => Map(word -> (i + 1))).getOrElse(Map(word -> 1))
      sender() ! cache.get(word).get
      
    case _ =>
      log.error(s"##### StateWorker receive message that not expected ")
  }
  
}