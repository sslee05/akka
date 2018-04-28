package com.sslee.faulttolerance.case02

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.actor.AllForOneStrategy
import akka.actor.SupervisorStrategy.{Stop, Restart, Resume, Escalate}
import akka.actor.Terminated
import akka.actor.OneForOneStrategy
import akka.actor.DeadLetter

object LogProcessingSupervisor {
  
  def props(sources: Vector[String], databaseUrls: Vector[String]) = 
    Props(new LogProcessingSupervisor(sources, databaseUrls))
    
  val name = "file-watcher-supervisor" 
}

class LogProcessingSupervisor(sources: Vector[String], databaseUrls: Vector[String]) 
  extends Actor with ActorLogging {
  
  val deadLetterActor = context.actorOf(Props[DeadLetterListener])
  val deadLetterListener = context.system.eventStream.subscribe(deadLetterActor, classOf[DeadLetter])
  
  val fileWatchers = sources.zip((1 to 5)).map {source => 
    val fileWatcher = context.actorOf(FileWatcher.props(source._1,source._2, databaseUrls))
    context.watch(fileWatcher)
  }
  
  import Messages._
  override def supervisorStrategy = OneForOneStrategy() {
    case _: DiskError => 
      log.info(s"#####${this} receive supervisorStrategy message DiskError will Stop ")
      Stop
  }
  
  import FileWatcher._
  def receive = {
    case f @ FileData(file) =>
      log.info(s"#####${this} receive message Start(${file}) ")
      fileWatchers.foreach(actor => actor ! NewFile(f))
    case Terminated(fileWatcher) =>
      log.info(s"#####${this} receive message Terminated(${fileWatcher}) ")
      val othersFileWatchers = fileWatchers.filterNot(_ != fileWatcher)
      if(othersFileWatchers.isEmpty)
        log.info(s"#####${this} system shutdown!!!! ")
        context.system.terminate()
  }
  
  override def preStart() = {
    log.info(s"#####${this} called preStasrt hock")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) = {
    log.info(s"#####${this} called preRestart hock. will call postStop if children exist of this actor")
    super.preRestart(reason, message)
  }
  
  override def postStop() = {
    log.info(s"#####${this} called postStop hock")
  }
  
  override def postRestart(reason: Throwable) = {
    log.info(s"#####${this} called postRestart. will call preStart")
  }
  
}