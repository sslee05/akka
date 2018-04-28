package com.sslee.remote

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.{Identify,ActorIdentity}
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.ReceiveTimeout

class RemoteLookupProxy(path: String) extends Actor with ActorLogging {
  
  //3초 동안 아무 메시지도 도착하지 않으면 ReceiveTimeout 메시지를 보낸다. 
  context.setReceiveTimeout(3 seconds)
  
  def receive = identify
  
  //Identify message를 보내 원격 Actor를 찾는다.
  def sendIdentifyRequest(): Unit = {
    log.debug(s"############ start actorSelection $path")
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }
  
  def identify: Receive = {
    //ActorIdentify[(String, Option[ActorRef]] 형태로 actor의 path정보와 원격 ActorRef가 들어 있다.
    case ActorIdentity(`path`, Some(actor)) =>  
      context.setReceiveTimeout(Duration.Undefined)
      log.debug("############switching to active state")
      context.become(active(actor))
      context.watch(actor)
      
    case ActorIdentity(`path`, None) =>
      log.error(s"##########Remote actor with path $path is not available.")
      
    case ReceiveTimeout => 
      sendIdentifyRequest()
      
    case msg : Any => 
      log.error(s"#########Ignoring message $msg. not ready yet.")
  }
  
  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) => 
      log.debug(s"#########Actor $actorRef terminated.")
      context.become(identify)
      log.debug("swiching to identify state")
      context.setReceiveTimeout(3 seconds)
      sendIdentifyRequest()
      
    case msg : Any => actor forward msg
  }
  
}