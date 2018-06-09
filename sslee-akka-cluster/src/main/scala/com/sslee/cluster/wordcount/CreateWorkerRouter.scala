package com.sslee.cluster.wordcount

import akka.actor.Actor
import akka.actor.ActorRef
import akka.routing.BroadcastPool
import akka.cluster.routing.ClusterRouterPool
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.actor.Props

trait CreateWorkerRouter { this: Actor =>
  
  def createWorkerRouter: ActorRef = 
    context.actorOf(
     ClusterRouterPool(
         BroadcastPool(10),
         ClusterRouterPoolSettings(
           totalInstances = 1000,
           maxInstancesPerNode = 20,
           allowLocalRoutees = false,
           useRole = None
         )).props(Props[JobWorker]),
         name = "workerRouter")
}