package com.sslee.cluster.router.group

import akka.actor.Actor
import akka.actor.ActorRef

class TestActor(stateService: ActorRef) extends Actor {
  def receive = {
    case msg =>
      println(s"######### start =>$msg")
      stateService ! msg
  }
}