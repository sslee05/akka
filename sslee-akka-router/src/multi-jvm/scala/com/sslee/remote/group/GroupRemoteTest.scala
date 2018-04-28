package com.sslee.remote.group

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import com.sslee.remote.MultiJvmBeforeAndAfterAll
import akka.actor.Props
import com.sslee.group.local.RouteeCreateActor
import akka.routing.RoundRobinGroup
import com.sslee.group.remote.SenderActor

class GroupRemoteTestMultiJvmFrontend extends GroupRemoteTest
class GroupRemoteTestMultiJvmBackend extends GroupRemoteTest

class GroupRemoteTest extends MultiNodeSpec(GroupMultiNodeConfig) 
  with MultiJvmBeforeAndAfterAll with ImplicitSender {
  
  
  import GroupMultiNodeConfig._
  import com.sslee.group.routemessages._
  
  val backendNode = node(backend)
  
  def initialParticipants = roles.size
  
  
  "group by in remote envoriment" must {
    
    "wait for all nodes to enter a barrier" in {
      //참여하는 모든 node를 시작 한다.
      enterBarrier("startup")
    }
    
    "remote construct and send and receive" in {
      
      runOn(backend) {
        
        val createActor = system.actorOf(Props(new RouteeCreateActor(2)),"createActor")
        println(s"createActor ${createActor.path}")
        createActor ! ""
        enterBarrier("deployed")
      }
      
      runOn(frontend) {
        
        enterBarrier("deployed")
        
        val path = List(
            node(backend) / "createActor" / "myRoutee-0",
            node(backend) / "createActor" / "myRoutee-1" ).map(actorPath => actorPath.toString)
        
        println(s"################ path: $path")
        val router = system.actorOf(RoundRobinGroup(path).props,"remoteRouter")
        
        router ! MyMessage("hellow remote routee")
        
        val senderActor = system.actorOf(SenderActor.props(router),SenderActor.name)
        senderActor ! MyReplyMessage("hellow remote routee") 
        //expectMsg()
      }
      
      enterBarrier("finished")
    }
  }
  
}