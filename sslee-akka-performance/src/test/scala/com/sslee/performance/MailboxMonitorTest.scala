package com.sslee.performance

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import org.scalatest.BeforeAndAfterAll
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import com.sslee.performance.messages._
import com.sslee.performance._
import scala.concurrent.duration._
import akka.actor.Props

class MailboxMonitorTest extends WordSpecLike  with MustMatchers with BeforeAndAfterAll {
  
  val config = ConfigFactory.load("mailbox-monitor")
  implicit val system = ActorSystem("mailboxMonitorTest",config)
  
  override protected def afterAll() {
    super.afterAll()
    system.terminate()
  }
  
  "mailboxMonitor" must {
    "expect time " in {
      val stateProbe = TestProbe()
      system.eventStream.subscribe(stateProbe.ref, classOf[MailboxStatistics])
      
      val testActor = system.actorOf(Props(new ServiceActor01(1 seconds)).withDispatcher("my-dispatcher"),"serviceActor")
      
      stateProbe.send(testActor, "message01")
      stateProbe.send(testActor, "message02")
      stateProbe.send(testActor,"message03")
      
      val stat = stateProbe.expectMsgType[MailboxStatistics]
      stat.queueSize must be(1)
      
      val stat2 = stateProbe.expectMsgType[MailboxStatistics]
      stat2.queueSize must (be(2) or be(1))
      
      val stat3 = stateProbe.expectMsgType[MailboxStatistics]
      stat3.queueSize must (be(3) or be(2))
      
    }
  }
  
}