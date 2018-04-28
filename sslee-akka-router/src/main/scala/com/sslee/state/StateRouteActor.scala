package com.sslee.state

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.state.routemessages._
import akka.actor.ActorRef

class StateRouteActor(rightActorRef: ActorRef, leftActorRef: ActorRef) extends Actor with ActorLogging {
  
  def receive = receiveLeft
  
  def receiveRight: Receive = {
    
    case RouteStateRight => 
      log.debug("already state right")
    
    case RouteStateLeft =>
      log.debug("change status RouteStateOn to RouteStateOff")
      context.become(receiveLeft)
      
    case MyMessage(msg) =>
      rightActorRef ! msg
  }
  
  def receiveLeft: Receive = {
    
    case RouteStateRight =>
      log.debug("change status RouteStateOff to RouteStateOn")
      context.become(receiveRight)
      
    case RouteStateLeft =>
      log.debug("already state left")
      
    case MyMessage(msg) => 
      leftActorRef ! msg
  }
  
  
  
}