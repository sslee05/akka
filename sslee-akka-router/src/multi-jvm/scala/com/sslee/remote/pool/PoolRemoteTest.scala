package com.sslee.remote.pool

import akka.remote.testkit.MultiNodeSpec
import com.sslee.remote.MultiJvmBeforeAndAfterAll
import akka.testkit.ImplicitSender
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.AddressFromURIString
import akka.remote.routing.RemoteRouterConfig
import akka.routing.RoundRobinPool


//router가 있는 front node 의 명세서 class 의 name 규칙은 
//extends 한 MultiNodeSpec명  + MultiJvm + nodeConfig에 선언한 role의 명칭  
class PoolRemoteTestMultiJvmFrontend extends PoolRemoteTest

//router가 있는 backend node 의 명세서 class 의 name 규칙은 
//extends 한 MultiNodeSpec명 + MultiJvm + nodeConfig에 선언한 role의 명칭
class PoolRemoteTestMultiJvmBackend extends PoolRemoteTest


class PoolRemoteTest extends MultiNodeSpec(PoolMultiNodeConfig) 
  with MultiJvmBeforeAndAfterAll with ImplicitSender {
  
  import PoolMultiNodeConfig._
  
  val backendNode = node(backend)
  //multi node에 함여하는 갯수 이는 MultiNodeSpec 의 abstract method
  //val actorSystem = ActorSystem("backend",ConfigFactory.load("remote-pool-backend"))
  
  
  import com.sslee.pool.routemessges._
  import com.sslee.pool.local._
  
  
  def initialParticipants = roles.size
  
  "router by pool in remote envoriment" must {
    
    "wait for all nodes to enter a barrier" in {
      //참여하는 모든 node를 시작 한다.
      enterBarrier("startup")
    }
    
    "start operation route by pool in remote " in {
      
      //backend node 즉 routee가 실행될 jvm에서 할 일들을 기술 한다. 
      runOn(backend) {
        //여기서는 routee 생성을 하지 않으므로 하는 것 없이 준비만 알린다.
        //backend node에서 Acto를 생성하여 frontend에서 actorSelection를 통해 
        //얻을 수 있지만 frontend 에서 backend node로 원격 배포를 할 수없는 것 같다. 
        enterBarrier("deployed")
      }
      
      //router가 있을 fontend node 의 jvm에서 할 것들을 정의 
      runOn(frontend) {
        
        //backend node 가 준비완료 되기를 기다린다.
        enterBarrier("deployed")
        
        //address 주소는 mult-jvm 에서 생성하는 주소를 가지고 해야 한다.
        //설정 file를 읽는 것이 아니므로 위에 address도 backend.node로 부터 얻는다.
        val addresses = Seq(
          //AddressFromURIString("akka.tcp://backend@0.0.0.0:2551")
          backendNode.address
        )
        
        //backend node에 배포를 할 수 없는 듯 하다.
        //결국 router의 원격 배포에 대한 multi-jvm:test는 성공 할 수 없었다.
        val router = system.actorOf(
            RemoteRouterConfig(RoundRobinPool(5),addresses).props(Props[MyRouteeActor]),"remoteRouter")
            
        router ! MyMessage("hellow routee")
        
        expectMsg(MyReplyMessage("hellow routee"))
      }
      
      enterBarrier("finished")
    }
  }
  
}