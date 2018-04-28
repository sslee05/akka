package com.sslee.group.local

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Props

class MyRouteeActor extends Actor with ActorLogging {
  
  import com.sslee.group.routemessages._
  
  def receive = {
    case MyMessage(msg) => 
      log.debug(s"######### $this receive message $msg")
      //routee에서의 sender는 router 입장에서의 sender를 가리킨다.
      sender() ! MyReplyMessage(msg)
  }
  
}

