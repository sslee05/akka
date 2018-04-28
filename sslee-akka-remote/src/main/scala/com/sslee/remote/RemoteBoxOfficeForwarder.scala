package com.sslee.remote

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.util.Timeout
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.actor.ActorRef
import akka.actor.Terminated

object RemoteBoxOfficeForwarder {
  def props(implicit timeout: Timeout) = {
    Props(new RemoteBoxOfficeForwarder)
  }
  
  def name = "forwarder"
}

class RemoteBoxOfficeForwarder(implicit timeout: Timeout) extends Actor with ActorLogging {
  
  import scala.concurrent.duration._
  import RemoteBoxOfficeForwarder._
  
  context.setReceiveTimeout(3 seconds)
  
  def receive = deploying
  
  def depolyAndWatch(): Unit = {
    //원격지에 배포하고 actor 참조를 얻는다.
    //하지만 원격지에 성공적으로 생성을 하든 실패하든 ActorRef가 생성되며 성공적 생성되었는지는 알 수 없다.
    //따라서 생성에 실패된  ActorRef 는 watch로 인해 무수한 Terminate message 가 발생되어 성능에 이슈가 있다. 
    val actor = context.actorOf(BoxOffice.props, BoxOffice.name)
    context.watch(actor)
    log.info("#####switching to maybe active state")
    context.become(maybeActive(actor))
    context.setReceiveTimeout(Duration.Undefined)    
  }
  
  def deploying: Receive = {
    case ReceiveTimeout => 
      log.info("#####boxOffice Actor deploy requestTimeout occur!")
      depolyAndWatch
      
    case msg: Any => 
      log.error(s"Ignoring message $msg, remote actor is not ready yet.")
  }
  
  def maybeActive(actor: ActorRef): Receive = {
    case Terminated(actorRef) => 
      log.info(s"Actor $actorRef terminated.")
      log.info("switching to deploying state")
      context.become(deploying)
      context.setReceiveTimeout(3 seconds)
      depolyAndWatch
      
    case msg:Any =>
      log.info(s"#####RemoteBoxOfficeForwarder receive message $msg")
      actor forward msg
  }
  
}