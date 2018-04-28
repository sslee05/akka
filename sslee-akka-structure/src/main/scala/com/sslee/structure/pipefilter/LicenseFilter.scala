package com.sslee.structure.pipefilter

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

class LicenseFilter(pipe: ActorRef) extends Actor with ActorLogging {
  
  import Message._
  def receive = {
    case msg: Photo =>
      log.debug(s"$self receive message $msg")
      if(!msg.license.isEmpty())
        pipe ! msg
  }
}

object LicenseFilter {
  def props(actorRef: ActorRef) = Props(new LicenseFilter(actorRef))
  def name = "licenseFilter"
}