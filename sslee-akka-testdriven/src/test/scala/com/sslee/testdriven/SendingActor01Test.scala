package com.sslee.testdriven

import akka.testkit.TestKit
import akka.actor.ActorSystem
import aia.testdriven.StopSystemAfterAll
import org.scalatest.WordSpecLike
import com.sslee.testdriven.chapter01.SendingActor
import scala.util.Random
import org.scalatest.MustMatchers

class SendingActor01Test extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "A SendingActor" must {
    "send a message to another actor when is has finished processing" in {
      import com.sslee.testdriven.chapter01.SendingActor._
      
      val props = SendingActor.props(testActor)
      val sendingActor = system.actorOf(props,"sendingActor")
      
      val size = 1000
      val maxInclusive = 100000
      
      def randomEvents() = (0 until size).map{ _ =>
        Event(Random.nextInt(maxInclusive))
      }.toVector
      
      val unsorted = randomEvents()
      val sortEvents = SortEvents(unsorted)
      sendingActor ! sortEvents
      
      
      //expectMsg와 다른점은 expectMsgPF는 
      //이는 testActor 의 receive처럼 부분 함수를 인자로 받는다
      //testActor에 전달된 SortedEvent 메시지와 매치된다.
      expectMsgPF() {
        case SortedEvents(events) => 
          events.size must be(size)
          unsorted.sortBy(_.id) must be(events)
      }
    }
  }
  
}