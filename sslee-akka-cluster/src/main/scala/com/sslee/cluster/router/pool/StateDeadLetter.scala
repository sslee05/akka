package com.sslee.cluster.router.pool

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.DeadLetter

class StateDeadLetter extends Actor with ActorLogging {
  
  import context._
  
  override def preStart() {
    system.eventStream.subscribe(self,classOf[DeadLetter])
    super.preStart()
  }
  
  override def postStop() {
    system.eventStream.unsubscribe(self, classOf[DeadLetter])
    super.postStop()
  }
  
  def receive = {
    case DeadLetter(msg, snd, rec) => 
      log.debug(s"######DeadLetter DeadLetter($msg,$snd,$rec)")
  }
  
}