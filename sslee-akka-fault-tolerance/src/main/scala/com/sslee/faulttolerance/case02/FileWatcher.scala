package com.sslee.faulttolerance.case02

import Messages._
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.{Stop,Restart,Resume,Escalate}
import akka.actor.PoisonPill
import akka.actor.Terminated
import akka.actor.DeadLetter

object FileWatcher {

  def props(source: String,num: Int,databaseUrls: Vector[String]) = Props(new FileWatcher(source,num,databaseUrls))
  
  case class NewFile(file: FileData)
  case class SourceAbandoned(uri: String)
}


class FileWatcher(source: String,num: Int, databaseUrls: Vector[String]) extends Actor with ActorLogging {
  
  //val deadLetterActor = context.actorOf(Props[DeadLetterListener])
  //val deadLetterListener = context.system.eventStream.subscribe(deadLetterActor, classOf[DeadLetter])
  
  override def supervisorStrategy = OneForOneStrategy() {
    case _ : CorruptedFileException if num == 2 => 
      log.info(s"#####${this} ${num} receive supervisorStrategy message CorruptedFileException will Resume ")
      Resume
    case _: CorruptedFileException =>
      log.info(s"#####${this} ${num} receive supervisorStrategy message CorruptedFileExceptin with Restart")
      Restart
  }
  
  val logProcessor = context.actorOf(LogProcessor.props(databaseUrls,num), LogProcessor.name)
  context.watch(logProcessor)
  
  import FileWatcher._
  import Messages._
  
  def receive = {
    case NewFile(file) => 
      log.info(s"#####${this} ${num} receive message NewFile(${file}) ")
      if(num == 2) throw new DiskError(s" ${num} number 2 DiskError")
      
      log.info(s"#####${this} ${num}  tell LogProcessor NewFile(${file})")
      logProcessor ! LogProcessor.LogFile(file)
      
    case SourceAbandoned(uri) if uri == source => 
      log.info(s"#####${this} ${num} receive message SourceAbandoned(${uri}) self ! PoisonPill")
      self ! PoisonPill
    case Terminated(`logProcessor`) => 
      log.info(s"#####${this} ${num} receive message Terminated(${logProcessor}) self ! PoisonPill ")
      self ! PoisonPill
  }
  
  override def preStart() = {
    log.info(s"#####${this} ${num} called preStart hock")
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) = {
    log.info(s"#####${this} ${num} called preRestart. will call postStop if children exist of this actor")
    super.preRestart(reason, message)
  }
  
  override def postStop() = {
    log.info(s"#####${this} ${num} called postStop")
  }
  
  override def postRestart(reason: Throwable) = {
    log.info(s"#####${this} ${num} called postRestart hock. will call preStart")
    super.postRestart(reason)
  }
  
}