package com.sslee.performance

package object messages {

  import akka.dispatch.Envelope
  
  case class MonitorEnvelope(queueSize: Int, receiver: String, entryTime: Long, handle: Envelope)
  case class MailboxStatistics(queueSize: Int, receiver: String, sender: String, entryTime: Long, exitTime: Long)
  
  case class ActorStatistics(receiver: String, sender: String, entryTime: Long, exitTime: Long)
  
}