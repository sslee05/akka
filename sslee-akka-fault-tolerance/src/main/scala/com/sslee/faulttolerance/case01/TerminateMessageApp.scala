package com.sslee.faulttolerance.case01

import akka.actor.ActorSystem
import akka.actor.Props

object TerminateMessageApp extends App {
  
  val system = ActorSystem("TerminateActorSystem")
  
  import MessageCase01._
  
  val actor = system.actorOf(Props(new ActorCase01Child01("actor01")),"actor01")
  actor ! InterruptMessage
}