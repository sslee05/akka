package com.sslee.performance

import akka.actor.ActorSystem
import akka.dispatch.MessageQueue
import akka.dispatch.UnboundedMessageQueueSemantics
import akka.event.LoggerMessageQueueSemantics
import messages._
import java.util.concurrent.ConcurrentLinkedQueue
import akka.actor.ActorRef
import akka.dispatch.Envelope

class MonitorQueue(val system: ActorSystem) extends MessageQueue 
  with UnboundedMessageQueueSemantics with LoggerMessageQueueSemantics {
  
  private final val queue = new ConcurrentLinkedQueue[MonitorEnvelope]
  
  def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
    val envel = MonitorEnvelope(
        queueSize = queue.size() + 1,
        receiver = receiver.toString(),
        entryTime = System.currentTimeMillis(),
        handle = handle)
        
    queue add envel
  }
  
  def dequeue() : Envelope = {
    val monitor = queue.poll()
    if(monitor != null ) {
      monitor.handle.message match {
        case stat: MailboxStatistics => //skip message
        case _ => 
          val stat = MailboxStatistics(
              queueSize = monitor.queueSize,
              receiver = monitor.receiver,
              sender = monitor.handle.sender.toString,
              entryTime = monitor.entryTime,
              exitTime = System.currentTimeMillis()
              )
              
          system.eventStream.publish(stat)
      }
      
      monitor.handle
    }
    else null
  }
  
  def numberOfMessages = queue.size()
  def hasMessages = !queue.isEmpty()
  
  def cleanUp(owner: ActorRef, deadLetters: MessageQueue): Unit = {
    if(hasMessages) {
      var envel = dequeue
      while(envel ne null) {
        deadLetters.enqueue(owner, envel)
        envel = dequeue
      }
    }
  }
  
}