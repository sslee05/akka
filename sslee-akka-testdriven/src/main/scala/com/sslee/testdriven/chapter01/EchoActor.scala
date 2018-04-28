package com.sslee.testdriven.chapter01

import akka.actor.Props
import akka.actor.Actor

class EchoActor extends Actor {
  
  def receive = {
    case msg => sender() ! msg
  }
}