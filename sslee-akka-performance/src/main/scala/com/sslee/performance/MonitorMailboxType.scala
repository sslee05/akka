package com.sslee.performance

import com.typesafe.config.Config
import akka.dispatch.MailboxType
import akka.actor.ActorSystem
import akka.dispatch.ProducesMessageQueue
import akka.actor.ActorRef
import akka.dispatch.MessageQueue

class MonitorMailboxType(settings: ActorSystem.Settings, config: Config) extends MailboxType 
  with ProducesMessageQueue[MonitorQueue] {
  
  final override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue = {
    system match {
      case Some(sys) => new MonitorQueue(sys) 
      case _ => throw new IllegalArgumentException("requires a system")
    }
  }
}