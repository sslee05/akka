package com.sslee.cluster


import com.sslee.cluster.messages._
import com.sslee.cluster.wordcount._
import akka.actor.Actor
import akka.actor.Props
import akka.routing.BroadcastPool
import akka.actor.ActorRef
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import akka.testkit.ImplicitSender
import com.sslee.cluster.StopSystemAfterAll

trait LocalWorkerRouteCreator extends CreateWorkerRouter { this: Actor =>
  
  override def createWorkerRouter: ActorRef = {
    context.actorOf(BroadcastPool(5).props(Props[JobWorker]), "worker-router")
  }
}

class TestJobMaster extends JobMaster with LocalWorkerRouteCreator
class TestReceptionist extends JobReceptionist(3) with MasterCreator {
  override def createMaster(name: String): ActorRef = context.actorOf(Props[TestJobMaster],name)
}

class LocalClusterWordCoutTest extends TestKit(ActorSystem("LocalWordCountTest")) 
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  val receptionist = system.actorOf(Props[TestReceptionist], "receptionist")
  
  "The word count " must {
    "count word" in {
      receptionist ! JobRequest("master-job01",List(
        "this is a test",
        "this is a test",
        "this is",
        "this"
      ))
      
      expectMsg(JobSucess("master-job01",Map("this" -> 4, "is" -> 3, "a" -> 2, "test" -> 2)))
    }
  }
  
}