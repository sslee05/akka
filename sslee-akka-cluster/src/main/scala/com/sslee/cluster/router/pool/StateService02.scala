package com.sslee.cluster.router.pool

import akka.actor.Props
import akka.actor.ActorLogging
import akka.routing.BroadcastPool
import akka.actor.Actor
import akka.cluster.routing.ClusterRouterPool
import akka.cluster.routing.ClusterRouterPoolSettings
import scala.concurrent.duration._
import akka.actor.Cancellable
import akka.actor.ActorRef

class StateService02 extends Actor with ActorLogging {
  
  import context._
  
  var count = 0
  
  //val workerRouter = context.actorOf(FromConfig.props(Props[StateWorker]),"workerRouter")
  //val tt = ClusterRouterConfig
  val workerRouter = { 
    context.actorOf(
      ClusterRouterPool(BroadcastPool(10),
          ClusterRouterPoolSettings(
              totalInstances = 1000,
              maxInstancesPerNode = 20,
              allowLocalRoutees = false,
              useRole = Some("worker"))
          ).props(Props[StateWorker02]),
      name = "stateWorkerRouter")
  }
  
  def receive = {
    case StateMessage(msg,replyTo) => 
      log.debug(s"##### StateService receive StateMessage StateMessage($msg,$replyTo)")
      workerRouter ! StateMessage(1 + msg, replyTo)
      
      Thread.sleep(5000L)
      workerRouter ! StateMessage(2 + msg, replyTo)
  }
  
}

case object StartTest
case class StateMessage(msg: String, replyTo: ActorRef)
