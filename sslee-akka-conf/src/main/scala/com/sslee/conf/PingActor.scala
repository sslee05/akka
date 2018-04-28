package com.sslee.conf

import akka.actor.Actor
import akka.event.Logging
import akka.actor.Props
import akka.actor.Terminated

class PingActor extends Actor {
  
  val log = Logging(context.system, this)
  
  val pongActor = context.actorOf(PongActor.props)
  context.watch(pongActor)
  
  import PingActor._
  def receive = {
    case Terminated(actorRef) =>
      log.debug(s"###USER-MSG: receive msg ChildActor Terminated. this Actor stopping...")
      context.stop(self)
    case `flag`  => pongActor ! "finish"
    case msg: String => 
      log.debug(s"###USER-MSG: receive msg $msg")
      pongActor ! "hellow pongActor"
  }
}

object PingActor {
  def props = Props(new PingActor)
  val flag = "finish"
}