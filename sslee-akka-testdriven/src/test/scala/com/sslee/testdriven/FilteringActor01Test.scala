package com.sslee.testdriven

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import aia.testdriven.StopSystemAfterAll
import org.scalatest.WordSpecLike
import com.sslee.testdriven.chapter01.FilteringActor

class FilteringActor01Test extends TestKit(ActorSystem("testsystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "A filtering Actor" must {
    "filter out particular message" in {
      
      import com.sslee.testdriven.chapter01.FilteringActor._
      
      val props = FilteringActor.props(testActor,5)
      val filter = system.actorOf(props,"filter-2")
      
      filter ! Event(1)
      filter ! Event(2)
      expectMsg(Event(1))
      expectMsg(Event(2))
      
      filter ! Event(1)
      //expectNoMsg는 아무 메시지도 도착하지 않았다는 것을 확인하기 위해 타임아웃이 발생할 때가지 
      //기다려야 한다. 따라서 test 시간이 좀 걸린다.
      expectNoMsg
      
      filter ! Event(3)
      expectMsg(Event(3))
      
      filter ! Event(1)
      expectNoMsg
      
      filter ! Event(4)
      filter ! Event(5)
      filter ! Event(5)
      expectMsg(Event(4))
      expectMsg(Event(5))
      expectNoMsg
    }
    
    "filter out particular messages" in {
      import com.sslee.testdriven.chapter01.FilteringActor._
      
      val props = FilteringActor.props(testActor, 5)
      val filterActor = system.actorOf(props,"filter-1")
      
      filterActor ! Event(1)
      filterActor ! Event(2)
      filterActor ! Event(1)
      filterActor ! Event(3)
      filterActor ! Event(1)
      filterActor ! Event(4)
      filterActor ! Event(5)
      filterActor ! Event(4)
      filterActor ! Event(6)
      
      //TestKit 에 있는  method로 
      //testActor가 받는 message를  case문이 더 이상 match되지 않을때가지 수집한다.
      val eventId = receiveWhile() {
        case Event(id) if id <= 5  => id
      }
      
      eventId  must be(List(1,2,3,4,5))
      expectMsg(Event(6))// systemActor를 끝내기 위해 
      
    }
  }
  
}