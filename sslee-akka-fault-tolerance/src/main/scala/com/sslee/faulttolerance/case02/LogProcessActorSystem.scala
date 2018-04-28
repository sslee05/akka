package com.sslee.faulttolerance.case02

import akka.actor.ActorSystem

object LogProcessActorSystem extends App {
  
  val system = ActorSystem("logProcessing")
  
  val databaseUrls = Vector(
    "http://mydatabase1", 
    "http://mydatabase2",
    "http://mydatabase3"    
  )
  
  val sources = Vector(
    "file:///source1/",
    "file:///source2/",
    "file:///source3/"
  )
  
  val startActor = system.actorOf(
      LogProcessingSupervisor.props(sources, databaseUrls),
      LogProcessingSupervisor.name
  )
  
  import Messages._
  
  val fileLine = (1 to 10).map(i =>
    FileLine((i.toString,"data_" + i))
  ).toVector
  
  val file = FileData(fileLine)
  
  startActor ! file
  
}