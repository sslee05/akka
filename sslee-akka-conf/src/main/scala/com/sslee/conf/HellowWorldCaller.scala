package com.sslee.conf

import scala.concurrent.duration.FiniteDuration
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging

class HellowWorldCaller(timer: FiniteDuration, actor: ActorRef) 
  extends Actor with ActorLogging {
  
  case class TimerTick(msg: String)
  
  override def preStart() {
    super.preStart()
    
    implicit val ec = context.dispatcher
    
    context.system.scheduler.schedule(
      timer, //schedule 이 처음 발생할 때까지의 시간
      timer, //매번 발생하는 schedule 사이의 간격 
      self,  // 메시지를 보낼 actor
      TimerTick("everyBody") //schedule 발생시 보낼 메시지 
    )
  }
    
    def receive = {
      case msg: String => log.info("receive {}", msg)
      case tick: TimerTick => actor ! tick.msg
    }
  
}