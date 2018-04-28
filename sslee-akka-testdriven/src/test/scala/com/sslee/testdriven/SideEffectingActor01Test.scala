package com.sslee.testdriven

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem

import SideEffectingActor01Test._
import org.scalatest.MustMatchers
import aia.testdriven.StopSystemAfterAll
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.testkit.CallingThreadDispatcher
import akka.actor.Props
import com.sslee.testdriven.chapter01.SideEffectingActor
import akka.testkit.EventFilter

class SideEffectingActor01Test extends TestKit(testSystem) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  import com.sslee.testdriven.chapter01.SideEffectingActor._
  
  "The SideEffectingActor" must {
    "say Hellow World! when a Greeting(World) is send to it" in {
      
      //단일 thread 환경 
      //원래 dispatcher는 별도의 thread에서 actor를 실행된다.
      //이는 actor를 실행하는 대신 test호출 중인 thread에서 actor를 호출 한다.
      //actor가 "World"라는 Greeing를 보낸 시점을 알야내고자 단일 thread 환경에서 실행
      val dispatcherId = CallingThreadDispatcher.Id
      val props = Props[SideEffectingActor].withDispatcher(dispatcherId)
      
      val actor = system.actorOf(props)
      //EventFilter는 log message를 걸러낸다. 
      EventFilter.info(message = "Hellow World!", occurrences = 1).intercept(
          actor ! Greeting("World")
      )
    }
  }
}

object SideEffectingActor01Test {
  val testSystem = {
    
    //TestKit는 로그에 기록되는 모든 이벤트를 처리하게 설정할 수 있는 
    //TestEventListener를 제공한다.
    val config = ConfigFactory.parseString(
      """
        akka.loggers = [akka.testkit.TestEventListener]
      """    
    )
    
    ActorSystem("testsystem", config)
  }
}