package com.sslee.cluster

import com.sslee.cluster._
import com.sslee.cluster.messages._
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import scala.concurrent.duration._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.actor.Props
import com.sslee.cluster.wordcount.JobReceptionist
import akka.cluster.ClusterEvent.MemberEvent

class WordsClusterSpecMultiJvmNode1 extends WordsClusterSpec
class WordsClusterSpecMultiJvmNode2 extends WordsClusterSpec
class WordsClusterSpecMultiJvmNode3 extends WordsClusterSpec
class WordsClusterSpecMultiJvmNode4 extends WordsClusterSpec


class WordsClusterSpec extends MultiNodeSpec(WordsClusterSpecConfig) 
  with MultiJvmBeforeAndAfterAll with ImplicitSender {
  
  def initialParticipants = roles.size
  
  import WordsClusterSpecConfig._
  
  val seedAddr = node(seed).address
  val masterAddr = node(master).address
  val worker1Addr = node(worker1).address
  val worker2Addr = node(worker2).address
  
  //???
  muteDeadLetters(classOf[Any])(system)
  
  "Word Count Cluster" must {
    "form the cluster" in within(10 seconds) {
      
      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])
      
      Cluster(system).join(seedAddr)
      
      //node 4개의 member에 대한 receive message 4개에 대한 확인
      receiveN(4).map{ case MemberUp(member) => member.address}.toSet must be(
        Set(seedAddr, masterAddr,worker1Addr,worker2Addr)    
      )
      
      Cluster(system).unsubscribe(testActor)
      
      enterBarrier("cluster-ready")
    }
    
    "execute a word count" in within(10 seconds) {
      runOn(master) {
        val receptionist = system.actorOf(Props(new JobReceptionist(3)),"receptionist")
        val text = List(
          "this is a test",
          "this is a test",
          "this is",
          "this"
        )
        
        receptionist ! JobRequest("job01", text)
        expectMsg(JobSucess("job01",Map("this" -> 4, "is" -> 3, "a" -> 2, "test" -> 2)))
      }
      
      enterBarrier("finish process word count")
    }
  }
}