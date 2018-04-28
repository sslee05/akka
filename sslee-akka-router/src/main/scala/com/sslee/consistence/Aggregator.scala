package com.sslee.consistence

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.consistence.routemessages._
import com.sslee.consistence.routemessages.MyMessage
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContextExecutor
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope

class Aggregator(timeout: FiniteDuration) extends Actor with ActorLogging {
  
  var messages = Map[String,MyMessage[String]]()
  implicit val ec: ExecutionContextExecutor = context.system.dispatcher
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    
    messages foreach (self ! _)
  }
  
  def receive = {
    case msg: MyMessage[String] => 
      messages.get(msg.id) match {
        case Some(message) => 
          log.debug(s"##### receive message $msg and send $msg")
          sender() ! MyMessageImpl(msg.id, message.values ++ msg.values)
          messages = messages - msg.id
        case None =>
          messages += msg.id -> msg
          context.system.scheduler.scheduleOnce(timeout, self, TimeoutMessage(msg.id))
      }
      
    case TimeoutMessage(id)  =>
      messages.get(id) match {
        case Some(previousMsg) => 
          sender() ! MyMessageImpl(id, previousMsg.values +:  "not completed message")
          messages = messages - id
        case None => 
          log.error(s"message missing id is $id")
      }
      
    case ConsistentHashableEnvelope(msg:MyMessage[String],key:String) =>
      messages.get(key) match {
        case Some(message) => 
          log.debug(s"##### receive message by ConsistentHashableEnvelope($msg,$key)")
          sender() ! MyMessageImpl(msg.id, message.values ++ msg.values)
          
        case None =>
          messages += key -> msg
          context.system.scheduler.scheduleOnce(timeout, self, TimeoutMessage(key))
      }
      
  }
  
}