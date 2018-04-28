package com.sslee.structure.scattergather

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorLogging

class Aggregator(timeout: FiniteDuration, pipe: ActorRef) extends Actor with ActorLogging {
  
  val messages = new ListBuffer[PhotoMessage]
  implicit val ec: ExecutionContextExecutor = context.system.dispatcher
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.debug(s"#####$self preRestart message $message reason $reason")
    super.preRestart(reason, message)
    
    messages foreach ( self ! _ )
    messages.clear
  }
  
  def receive = {
    case receiveMsg: PhotoMessage => 
      messages.find(m => m.id == receiveMsg.id) match {
        case Some(msg) =>
          log.debug(s"#####$self received message $receiveMsg find other arrived message $receiveMsg")
          pipe ! PhotoMessage (
             msg.id,
             msg.photo,
             receiveMsg.createTime orElse msg.createTime,
             receiveMsg.speed orElse msg.speed
          )
          messages -= msg
          
        case None => {
          log.debug(s"#####$self received message $receiveMsg is first arrieved message")
          messages += receiveMsg
          context.system.scheduler.scheduleOnce(timeout, self, TimeoutMessage(receiveMsg.id))
        }
      }
    
    case TimeoutMessage(timeoutMsg) => 
      messages.find(msg => msg.id == timeoutMsg) match {
        case Some(photoMessage) => 
          log.debug(s"#####$self sending not completed message to $pipe")
          pipe ! photoMessage
          
          messages -= photoMessage
          
        case None => 
          log.debug(s"$self ready sended message $timeoutMsg")
      }
      
    case ex: Exception => throw ex
  }
      
}

object Aggregator {
  def props(timeout: FiniteDuration, pipe: ActorRef) = Props(new Aggregator(timeout, pipe))
}