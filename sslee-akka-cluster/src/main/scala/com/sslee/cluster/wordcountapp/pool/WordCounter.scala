package com.sslee.cluster.wordcountapp.pool

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.cluster.wordcountapp.messages._
import akka.routing.Broadcast

class WordCounter extends Actor with ActorLogging {
  
  def receive = {
    case TaskData(text) =>
      log.info(s"#####WordCounter-processCount: receive TaskData($text)")
      if(!text.isEmpty()) {
    	    val xs = text.split(" ").toList
    	    sender() ! WordCount(xs.foldLeft[Map[String,Int]](Map.empty)((b,a) => b.updated(a, b.getOrElse(a,0) + 1)))
      }
      else sender() ! WordCount(Map.empty[String,Int])
  }
  
}