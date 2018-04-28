package com.sslee.structure.pipefilter

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import org.scalatest.fixture.TestSuite
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import scala.concurrent.duration._
import com.sslee.structure.StopSystemAfterAll

class PipeFilterTest extends TestKit(ActorSystem("pipeFilterSystem"))
  with WordSpecLike with MustMatchers with StopSystemAfterAll {

  import com.sslee.structure.pipefilter
  import com.sslee.structure.pipefilter.Message._

  "PipeFilter structus " must {
    "PipeFilter basic pattern" in {
      val endProbe = TestProbe()

      val speedFilterRef = system.actorOf(SpeedFilter.props(50, endProbe.ref), SpeedFilter.name)
      val licenseFilterRef = system.actorOf(LicenseFilter.props(speedFilterRef), LicenseFilter.name)

      val msg = Photo("123xyz", 60)
      licenseFilterRef ! msg
      endProbe expectMsg msg

      licenseFilterRef ! Photo("123xyz", 49)
      endProbe.expectNoMsg(1 seconds)
    }
    
    "sometime is exchange filter for performance " in {
      val endProbe = TestProbe()
      
      val licenseFilterRef = system.actorOf(LicenseFilter.props(endProbe.ref))
      val speedFilterRef = system.actorOf(SpeedFilter.props(50, licenseFilterRef))
      
      val msg = Photo("123xy",60)
      speedFilterRef ! msg
      endProbe expectMsg msg
      
      speedFilterRef ! Photo("123xy", 49)
      endProbe.expectNoMsg(1 seconds) 
    }
  }

}