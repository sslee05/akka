package com.sslee.performance

import akka.actor.ActorLogging
import akka.actor.Actor
import scala.concurrent.duration.Duration

class ServiceActor01(latencyTime: Duration) extends Actor with ActorLogging {
  
  def receive = {
    case msg =>
      Thread.sleep(latencyTime.toMillis)
  }
  
}