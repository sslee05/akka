package com.sslee.performance

import akka.actor.Actor
import com.sslee.performance.messages._

trait MyMonitorActor extends Actor {
  
  abstract override def receive = {
    case msg: Any =>
      val start = System.currentTimeMillis()
      super.receive(msg)
      val end = System.currentTimeMillis()
      
      val stat = ActorStatistics(
          self.toString,
          sender.toString,
          start,
          end)
          
      context.system.eventStream.publish(stat)
  }
}