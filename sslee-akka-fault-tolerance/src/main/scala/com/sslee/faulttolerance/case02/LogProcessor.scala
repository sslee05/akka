package com.sslee.faulttolerance.case02

import akka.actor.Actor
import akka.actor.ActorLogging
import java.io.File
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.{ Restart, Stop, Resume, Escalate }
import akka.actor.Props
import java.util.UUID
import Messages._
import akka.actor.Terminated
import akka.actor.PoisonPill
import scala.concurrent.duration._
import akka.pattern.BackoffSupervisor
import akka.pattern.Backoff
import akka.actor.DeadLetter

trait LogParsing {
  import DbWriter._
  // 로그 파일을 파싱한다. 로그 파일의 각 줄에서 Line 객체를 만든다.
  // 파일이 잘못된 경우에는 CorruptedFileException 예외를 던진다.
  def parse(file: FileData): Vector[DbWriter.Line] = {
    // 파서를 여기서 정의한다. 여기서는 가짜 file data로 한다.
    file.lines.map(fileLine => Line(fileLine.row._1.toLong, fileLine.row._2))
  }
}

trait FileWatchingAbilities {
  def register(uri: String): Unit = {

  }
}

object LogProcessor {

  def props(databaseUrls: Vector[String],num: Int) = Props(new LogProcessor(databaseUrls,num))
  def name = s"""log-processor_${UUID.randomUUID.toString}"""

  case class LogFile(file: FileData)
}

class LogProcessor(databaseUrls: Vector[String],num: Int) extends Actor with ActorLogging with LogParsing {

  require(databaseUrls.nonEmpty)
  
  //val deadLetterActor = context.actorOf(Props[DeadLetterListener])
  //val deadLetterListener = context.system.eventStream.subscribe(deadLetterActor, classOf[DeadLetter])

  val initialDataUrl = databaseUrls.head
  var alternateDatabases = databaseUrls.tail
  

  def backoffSupervisorProps(databaseUrl: String) = BackoffSupervisor.props(
    //Backoff.onStop(
    Backoff.onFailure(
      childProps = DbWriter.props(databaseUrl,num),
      childName = DbWriter.name(databaseUrl),
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2).withManualReset
      .withSupervisorStrategy(supervisorStrategy))

  //재시도시 연결 가능성 있을 경우 Restart, 가능성 없는 Exception 시 Stop, 나머지는 위로 올린다.
  //10초이내 5번 시도시 문제 해결이 되지 않으면 위로 올려 보낸다.
  //BackOffSupervisor 는 일정 지연시간 이후에 재시작 시도을 하게 한다.
  
  override def supervisorStrategy = OneForOneStrategy(
    maxNrOfRetries = 5,
    withinTimeRange = 10 seconds) {
    case _: DbBrokenConnectionException =>
      log.info(s"#####${this} ${num}  receive supervisorStrategy message DbBrokenConnectionException will Restart ")
      Restart
    case _: DbNodeDownException =>
      log.info(s"#####${this} ${num}  receive supervisorStrategy message DbNodeDownException will Stop ")
      Stop
  }
  
  var dbWriter = context.actorOf(DbWriter.props(initialDataUrl,num), DbWriter.name(initialDataUrl))
  //var dbWriter = context.actorOf(backoffSupervisorProps(initialDataUrl))
  context.watch(dbWriter)

  import LogProcessor._

  def receive = {
    case LogFile(file) =>
      log.info(s"#####${this} ${num}  receive message LogFile(${file}) tell dbWriter")
      
      val lines = parse(file)
      val illegalFile = lines.filter(line => (line.key == 2l) )
      if(!illegalFile.isEmpty) throw new CorruptedFileException(s"illegal ${num} file format",file)
      
      log.info(s"#####${this} ${num}  tell dbWriter LogFile(${file})")
      lines.foreach(lineData => dbWriter ! lineData)
      
    case Terminated(actorRef) =>
      log.info(s"#####${this} ${num}  receive message Terminated(${actorRef}) tell dbWriter")
      if (alternateDatabases.nonEmpty) {

        val newDatabaseUrl = alternateDatabases.head
        alternateDatabases = alternateDatabases.tail

        log.info(s"#####${this} ${num}  receive message Terminated create new DbWriter(${newDatabaseUrl})")

        dbWriter = context.actorOf(DbWriter.props(newDatabaseUrl,num), DbWriter.name(newDatabaseUrl))
        //dbWriter = context.actorOf(backoffSupervisorProps(newDatabaseUrl))
        context.watch(dbWriter)
      } else {
        log.info(s"#####${this} ${num}  receive message Terminated no more databaseUrl self ! PoisonPill")
        self ! PoisonPill
      }
  }

  override def preStart() = {
    log.info(s"#####${this} ${num}  called preStart")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    log.info(s"#####${this} ${num}  called preRestart hock. will call postStop if children exist of this actor")
    super.preRestart(reason, message)
  }

  override def postStop() {
    log.info(s"#####${this} ${num}  called postStop hock")
  }

  override def postRestart(reason: Throwable) = {
    log.info(s"#####${this} ${num}  called postRestart hock. will call preStart")
    super.postRestart(reason)
  }
}