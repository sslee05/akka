package com.sslee.faulttolerance

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.sslee.faulttolerance.case02._
import akka.testkit.TestActorRef

class Case02ActorTest extends TestKit(ActorSystem("testsystem")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {

  import com.sslee.faulttolerance.case02.Messages._

  "LogProcessSupervisor actor " must {
    "LogProcessSupervisor receive" in {

      val databaseUrls = Vector(
        "http://mydatabase1",
        "http://mydatabase2")

      val sources = Vector(
        "file:///source1/",
        "file:///source2/")

      val actor = system.actorOf(LogProcessingSupervisor.props(sources, databaseUrls), LogProcessingSupervisor.name)
      
      watch(actor)
      
      val fileLine = (1 to 10).map(i =>
        FileLine((i.toString,"data_" + i))
      ).toVector
      
      //val fileData = FileData(fileLine)
      val fileData = FileData(Vector(FileLine(("1","data_8"))))
      actor ! fileData
      
      val fileData2 = FileData(Vector(FileLine(("2","data_8"))))
      actor ! fileData2
      
      val fileData3 = FileData(Vector(FileLine(("3","data_8"))))
      actor ! fileData3
  
      Thread.sleep(5000l)
    }
  }

}