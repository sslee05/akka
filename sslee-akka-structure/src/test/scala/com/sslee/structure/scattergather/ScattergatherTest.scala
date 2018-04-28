package com.sslee.structure.scattergather

import org.scalatest.MustMatchers
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import com.sslee.structure.StopSystemAfterAll
import akka.testkit.TestProbe
import scala.concurrent.duration._
import java.util.Date
import akka.actor.actorRef2Scala
import org.scalactic.source.Position.apply
import scala.collection.Seq

class ScattergatherTest extends TestKit(ActorSystem("scattergather")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "Scattergather pattern " must {
    
    "recipient struct pattern " in {
      
      val probe01 = TestProbe()
      val probe02 = TestProbe()
      val probe03 = TestProbe()
      
      val xs = Seq(probe01.ref,probe02.ref,probe03.ref)
      val receipient = system.actorOf(RecipientList.props(xs))
      
      val msg = "message"
      receipient ! msg
      
      probe01 expectMsg msg
      probe02 expectMsg msg
      probe03 expectMsg msg
    }
    
    "aggregator gathering " in {
      
      val probe = TestProbe()
      
      //val recipientActRef = system.actorOf(RecipientList.props(recipients))
      val aggregatorActRef = system.actorOf(Aggregator.props(1 second, probe.ref))
      val photoStr = ImageProcessing.createPhotoString(new Date(),60)
      
      
      val msg01 = PhotoMessage("id1",
          photoStr,
          None,
          Some(60))
          
      val msg02 = PhotoMessage("id1",
          photoStr,
          Some(new Date()),
          None)
          
      aggregatorActRef ! msg01
      aggregatorActRef ! msg02
      
      probe expectMsg PhotoMessage("id1",
          photoStr,
          msg02.createTime,
          msg01.speed)
    }
    
    "aggregator receive uncompleted message" in {
      
      val probe = TestProbe()
      val photoStr = ImageProcessing.createPhotoString(new Date(),60)
      
      val aggregatorActRef = system.actorOf(Aggregator.props(1 seconds, probe.ref))
      
      val msg = PhotoMessage("id1",
          photoStr,
          None,
          Some(60))
          
      aggregatorActRef ! msg
      
      probe expectMsg msg
    }
    
    
    "scatter and gathering combine " in {
      val probe = TestProbe()
      
      //val recipientActRef = system.actorOf(RecipientList.props(recipients))
      val aggregatorActRef = system.actorOf(Aggregator.props(1 second, probe.ref))
      val speedActRef = system.actorOf(GetSpeed.props(aggregatorActRef))
      val timeActRef = system.actorOf(GetTime.props(aggregatorActRef))
      val receipientActRef = system.actorOf(RecipientList.props(Seq(speedActRef,timeActRef)))
      
      val photoStr = ImageProcessing.createPhotoString(new Date(),60)
      
      val msg = PhotoMessage("id1",
            photoStr,
            None,
            None)
            
      receipientActRef ! msg
      
      probe.expectMsgPF(3 seconds) {
        case PhotoMessage(id,name,Some(t),Some(s)) => println(s"time is $t speed is $s")
      }
    }
    
    "scatter and gather preRestart mesasge processing " in {
      
      val probe = TestProbe()
      
      val aggregatorActRef = system.actorOf(Aggregator.props(1 second, probe.ref))
      
      val photoStr = ImageProcessing.createPhotoString(new Date(), 60)
      
      val msg = PhotoMessage("id1",
          photoStr,
          Some(new Date()),
          None
      )
      
      aggregatorActRef ! msg 
      
      aggregatorActRef ! new IllegalStateException("restart test")
      
      val msg2 = PhotoMessage("id1",
          photoStr,
          None,
          Some(60)
      )
      
      aggregatorActRef ! msg2
      
      probe expectMsg PhotoMessage("id1",
          photoStr,
          msg.createTime,
          msg2.speed
      )
          
    }
    
    
  }
  
}