package com.sslee.cluster.wordcount

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.event.Logging
import akka.cluster.Cluster

object WorkerNodeApp extends App {
  
  val config = ConfigFactory.load("worker")
  implicit val system = ActorSystem("wordcount",config)
  implicit val ec = system.dispatcher
  
  //system.eventStream.setLogLevel(Logging.DebugLevel)
  
  //Cluster(system)
}