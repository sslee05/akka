package com.sslee.group.remote

import akka.actor.Actor
import akka.actor.ActorLogging
import com.sslee.group.routemessages._
import akka.actor.ActorRef
import akka.actor.Props

class SenderActor(actorRef: ActorRef) extends Actor with ActorLogging {
  
  def receive = {
    case MyReplyMessage(msg) => 
      log.debug(s"####### $self receive replay message $msg")
    case MyMessage(msg) => 
      log.debug(s"####### $self receive message $msg and forward route message")
      actorRef ! MyMessage(s"$msg I am senderActor in Frontend")
  }
}

object SenderActor {
  def props(actorRef: ActorRef) = Props(new SenderActor(actorRef))
  def name = "senderActor"
}