package com.sslee.cluster.router.group

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import com.sslee.cluster.router.messages._
import akka.cluster.Cluster
import akka.actor.Actor
import com.sslee.cluster.router.group._


object SimpleRouterClusterViaGroupApp {
  
  def main(args: Array[String]): Unit = {
    if(args.isEmpty)
      startup(Seq("2551","2552","2553"))
    else 
      startup(args)
  }
  
  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory.parseString(s"""
          akka.remote.netty.tcp.port=$port
        """).withFallback(ConfigFactory.load("ex-router-group"))
        
      val system = ActorSystem("ClusterSystem",config)
      //Cluster(system)
      system.actorOf(Props[StateWorker],"stateWorker")
      
      if(port != "2551" && port != "2552") {
        
        val stateService = system.actorOf(Props[StateService],"stateService")  
        val testActor = system.actorOf(Props(new TestActor(stateService)),"testActor")
        testActor ! StatesJob(
            """
            First we prepare a reusable Flow that will 
            change each incoming tweet into an integer of value 1. 
            We’ll use this in order to combine those with a Sink.fold 
            that will sum all Int elements of 
            the stream and make its result available as a Future[Int]. 
            Next we connect the tweets stream to count with via. Finally 
            we connect the Flow to the previously prepared Sink using toMat.
            """
        )
      }
        
    }
    
  }
}

