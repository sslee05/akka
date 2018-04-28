package com.sslee.faulttolerance

import akka.actor.ActorSystem

object Case01App extends App {
  
  import com.sslee.faulttolerance.case01.ActorCase01
  import com.sslee.faulttolerance.case01.MessageCase01._
  
  val system = ActorSystem("testsystem-case01")
  val actor = system.actorOf(ActorCase01.props("parentActor"))
  
  actor ! Message("start Message")
  
  Thread.sleep(3000l)
  system.terminate()
}