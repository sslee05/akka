package com.sslee.pool.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import com.sslee.pool.local.SenderActor
import akka.routing.RoundRobinPool
import akka.remote.routing.RemoteRouterConfig
import akka.actor.AddressFromURIString
import com.sslee.pool.local.MyRouteeActor
import akka.routing.Broadcast

object RemotePoolFrontendBoot extends App {
  
  import com.sslee.pool.routemessges._
  
  val system = ActorSystem("frontend", ConfigFactory.load("remote-pool-frontend"))
  
  
  val addresses = Seq(
     AddressFromURIString(system.settings.config.getString("routee.address"))    
  )
  
  val router = system.actorOf(RemoteRouterConfig(RoundRobinPool(5),addresses)
      .props(Props[MyRouteeActor]),"myRouter")
      
  val senderActor = system.actorOf(Props(new SenderActor(router)),"senderActor")
      
  senderActor ! MyMessage("hellow Routee!")
  println("send message to all routees")
  senderActor ! Broadcast(MyMessage("hellow Routee!"))
}