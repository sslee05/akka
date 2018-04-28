package com.sslee.structure.scattergather

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

class GetSpeed(pipe: ActorRef) extends Actor with ActorLogging {
  
  def receive = {
    case msg: PhotoMessage => 
      log.debug(s"#####$self receive message $msg")
      val speed:Option[Int] = ImageProcessing.getSpeed(msg.photo)
      pipe ! msg.copy(speed = speed)
  }
}

object GetSpeed {
  def props(pipe: ActorRef) = Props(new GetSpeed(pipe))
}