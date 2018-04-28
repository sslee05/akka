package com.sslee.pool.local

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.FromConfig

object LocalPoolBoot extends App {
  
  //ActorSystem boot
  val config = ConfigFactory.load("local-pool")
  val system = ActorSystem("localPool",config)
  
  // local pool 기반의 roundrobin-pool router 생성 
  val router = system.actorOf(FromConfig.props(Props[MyRouteeActor]),"localRouter")
  
  //senderActor: routee가 일을 처리 하고 결과 응답을 보낼 actor 
  val sender = system.actorOf(Props(new SenderActor(router)),"senderActor")
  
  //message 전송
  import com.sslee.pool.routemessges._
  router ! MyMessage("hellow Routee")
  
}