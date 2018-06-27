package com.sslee.performance

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import com.sslee.performance.messages._
import com.sslee.performance._
import akka.actor.Props
import scala.concurrent.duration._
import org.scalatest.matchers.MatchPatternMacro


class ActorMonitorTest extends WordSpecLike with MustMatchers with BeforeAndAfterAll {
  
  val config = ConfigFactory.load("monitor")
  implicit val system = ActorSystem("actorMonitorTest",config)
  
  override protected def afterAll() {
    super.afterAll()
    system.terminate()
  }
  
  "ActorMonitor" must {
    "ActorService period" in {
      val statProbe = TestProbe()
      system.eventStream.subscribe(statProbe.ref, classOf[ActorStatistics])
      
      val testActor = system.actorOf(Props(new ServiceActor01(1 seconds) with MyMonitorActor),"serviceActor")
      
      statProbe.send(testActor, "message")
      val stat = statProbe.expectMsgType[ActorStatistics]
      stat.exitTime - stat.entryTime must be (1000L +- 10)
    }
  }
  
  
}