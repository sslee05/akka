package com.sslee.group.remote

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.routing.RoundRobinGroup
import com.sslee.group.remote._
import com.sslee.group.routemessages._
import akka.actor.Props
import akka.routing.FromConfig

object RemoteGroupFrontendBoot extends App {
  
  val config = ConfigFactory.load("remote-group-frontend")
  val system = ActorSystem("frontend",config)
  
  
  import scala.collection.JavaConverters._
  
  //val paths = config.getList("remoteroutee.path").asScala.toList.map(value => value.render())
  //println(s"########path=>$paths")
  //val router = system.actorOf(RoundRobinGroup(paths).props,"remoteRouter")
  
  
  val router = system.actorOf(FromConfig.props,"remoteRouter")
  val senderActorRef = system.actorOf(SenderActor.props(router),SenderActor.name)
  
  senderActorRef ! MyMessage("hellow remote routee!")
  //router ! MyMessage("hellow remote routee!")
}