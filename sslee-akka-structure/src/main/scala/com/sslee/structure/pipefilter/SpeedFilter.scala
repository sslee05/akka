package com.sslee.structure.pipefilter

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

class SpeedFilter(minSpeed: Int, pipe: ActorRef) extends Actor with ActorLogging {
  
  import Message._
  
  def receive = {
    case msg: Photo => 
      log.debug(s"$self receive message $msg")
      if(msg.speed > minSpeed)
        pipe ! msg
  }
}

object SpeedFilter {
  def props(minSpeed: Int, pipe: ActorRef) = Props(new SpeedFilter(minSpeed,pipe))
  def name = "speedFilter"
}