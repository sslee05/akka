package com.sslee.cluster.wordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberUp,MemberExited,MemberRemoved,UnreachableMember,ReachableMember,CurrentClusterState}
import akka.cluster.MemberStatus

class ClusterDomainEventListener extends Actor with ActorLogging {
  
  override def postStop() = {
    //구독 해제 
    Cluster(context.system).unsubscribe(self)
    super.postStop()
  }
  
  override def preStart() {
    //ClusterDomainEvent에 대한 구독 시작 
    Cluster(context.system).subscribe(self,classOf[ClusterDomainEvent])
  }
  
  def receive = {
    case MemberUp(member) => log.info(s"#####$member UP")
    case MemberExited(member) => log.info(s"#####$member exited")
    case MemberRemoved(member,previousState) => 
      if(previousState == MemberStatus.Exiting)
        log.info(s"#####$member gracefully removed")
      else
        log.info(s"#####$member downed after unreachable removed")
        
    case UnreachableMember(member) => log.info(s"#####$member unreachable")
    case ReachableMember(member) => log.info(s"member reachable")
    case s: CurrentClusterState => log.info(s"cluster state: $s")
  }
  
}