package com.sslee.pool.local

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import com.sslee.StopSystemAfterAll
import org.scalatest.WordSpecLike
import akka.testkit.TestProbe
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import akka.routing.Broadcast
import akka.routing.RoundRobinPool
import akka.pattern.ask
import akka.util.Timeout
import akka.routing.BalancingPool
import akka.actor.Props
import akka.testkit.ImplicitSender

class LocalPoolTest extends TestKit(ActorSystem("localPool",ConfigFactory.load("local-pool"))) 
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "router by pool in local" must {
    
    import com.sslee.pool.routemessges._
    
    "using router by config" in {
      
      //router 생성 
      val router = system.actorOf(FromConfig.props(Props[MyRouteeActor]),"localRouter")
      //code에서 생성
      //val router2 = system.actorOf(BalancingPool(2).props(MyRouteeActor.props(testActor)),"localRouter")
      
      //message 를 보내보기
      router ! MyMessage("hellow routee")
      
      //검증 
      expectMsg(MyReplyMessage("hellow routee"))
      
      //Broadcase를 통해 전체 메시지 보내기
      router ! Broadcast(MyMessage("hellow routee"))
      
      val rs = receiveWhile() {
        case MyReplyMessage(msg) => msg
      }
      
      //검증
      rs.size must be(2)
      
    }
    
    "using router by code" in {
      
      //router 생성 
      val router = system.actorOf(RoundRobinPool(5).props(Props[MyRouteeActor]),"localRouter2")
      
      //message 를 보내보기 
      router ! MyMessage("hellow routee")
      
      //검증 
      expectMsg(MyReplyMessage("hellow routee"))
      
      //Broadcast를 통해 전체에게 메시지 보내기 
      router ! Broadcast(MyMessage("hellow routee"))
      
      val rs = receiveWhile() {
        case MyReplyMessage(msg) => msg
      }
      
      rs.size must be(5)
    }
  }
}