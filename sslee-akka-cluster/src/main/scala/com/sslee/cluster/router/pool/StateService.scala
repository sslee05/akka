package com.sslee.cluster.router.pool

import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.routing.FromConfig
import akka.actor.ActorRef
import com.sslee.cluster.router.messages._
import scala.concurrent.duration._
import akka.routing.BroadcastPool
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.cluster.routing.ClusterRouterPool


class StateService extends Actor with ActorLogging {
  
  //val workerRouter = context.actorOf(FromConfig.props(Props[StateWorker]),"workerRouter")
  //val tt = ClusterRouterConfig
  lazy val workerRouter = { 
    context.actorOf(
      ClusterRouterPool(BroadcastPool(10),
          ClusterRouterPoolSettings(
              totalInstances = 1000,
              maxInstancesPerNode = 20,
              allowLocalRoutees = false,
              useRole = Some("worker"))
          ).props(Props[StateWorker]),
      name = "stateWorkerRouter")
  }
  
  def receive = {
    case StatesJob(text) => 
      log.debug(s"##### StateService receive message StatesJob($text)")
      if(text != "") {
        val words = text.split(" ")
        val replyTo = sender()
        val stateAggregator = context.actorOf(Props(classOf[StateAggregator],words.size, replyTo))
        words.foreach{ word =>
          log.debug(s"workerRouter path ${workerRouter.path}")
    	      workerRouter.tell(word, stateAggregator)
    	    }        
      }
  }
  
}

class StateAggregator(expectedResults: Int, replyTo: ActorRef) extends Actor with ActorLogging {
  
  var results = Seq.empty[Int]
  context.setReceiveTimeout(3 seconds)
  
  def receive = {
    case wordCount: Int =>
      log.debug(s"##### StateAggregator receive message $wordCount")
      results = results :+ wordCount
      if(expectedResults == results.size) {
        val meanWordLength = results.sum.toDouble / results.size
        replyTo ! StateResult(meanWordLength)
        context.stop(self)
      }
  }
}