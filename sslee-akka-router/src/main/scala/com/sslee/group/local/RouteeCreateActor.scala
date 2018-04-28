package com.sslee.group.local

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.sslee.group.routemessages._
import akka.actor.Terminated

class RouteeCreateActor(nrActors: Int) extends Actor with ActorLogging {
  
  override def preStart() {
    
    log.debug(s"######RouteeCreateActor call preStarting... and create routees")
    super.preStart()
    
    (0 until nrActors).map { n => 
      val routee = context.actorOf(Props[MyRouteeActor],s"myRoutee-$n")
      context watch routee
    }
  }
  
  def receive = {
    //routee가 stop 되었을 경우 
    case Terminated(actorRef) => {
      log.debug(s"##### recieve message Terminated routee $actorRef")
      val actor = context.actorOf(Props[MyRouteeActor],actorRef.path.name)
      context.watch(actor)
    }
  }
  
}