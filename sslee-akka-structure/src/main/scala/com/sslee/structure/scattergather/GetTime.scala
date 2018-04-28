package com.sslee.structure.scattergather

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class GetTime(pipe: ActorRef) extends Actor with ActorLogging {
  
  def receive = {
    case msg: PhotoMessage => 
      log.debug(s"#####$self received message $msg")
      
      val time = ImageProcessing.getTime(msg.photo)
      pipe ! msg.copy(createTime = time)
  }
}

object GetTime {
  def props(pipe: ActorRef) = Props(new GetTime(pipe))
}