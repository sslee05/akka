package com.sslee.cluster.wordcount

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.cluster.Cluster

object MasterNodeApp extends App {
  
  val config = ConfigFactory.load("seed")
  val system = ActorSystem("wordcount",config)
  implicit val ec = system.dispatcher
  
  Cluster(system)
  
  //val receptionist = system.actorOf(Props(new JobReceptionist(3)),"receptionist")
  
  //system.eventStream.setLogLevel(Logging.DebugLevel)
  
  //Cluster(system)
  
}