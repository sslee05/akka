package com.sslee.faulttolerance.case02

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.DeadLetter

object DbWriter {
  
  def props(databaseUrl: String,num: Int) = Props(new DbWriter(databaseUrl,num))
  
  def name(databaseUrl: String) = 
    s"""db-writer-${databaseUrl.split("/").last}"""
  
  case class Line(key: Long, message: String)
  
}

class DbWriter(databaseUrl: String,num: Int) extends Actor with ActorLogging {
  
  //val deadLetterActor = context.actorOf(Props[DeadLetterListener])
  //val deadLetterListener = context.system.eventStream.subscribe(deadLetterActor, classOf[DeadLetter])
  
  var connection:MockDbCon = _
  
  import DbWriter._
  
  def receive = {
    case Line(key,message) => 
      log.info(s"#####${this} ${num} receive message Line(${key},${message}) execute query")
      connection.write(key)
    case deadMessage: DeadLetter => 
      log.info(s"#####${this} ${num} receive Dead Message ${deadMessage}")
  }
  
  override def preStart() = {
    log.info(s"#####${this} ${num} preStart hock called connection created")
    connection = new MockDbCon(databaseUrl)
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.info(s"#####${this} ${num} preReStart hock called. will call postStop if children exist of this actor")
    super.preRestart(reason, message)
  }
  
  override def postStop(): Unit = {
    log.info(s"#####${this} ${num} DbWriter callled postStop db connection is closed!")
    connection.close()
  }
  
  override def postRestart(reason: Throwable) {
    log.info(s"#####${this} ${num} postRestart hock. called will call preStart")
    super.postRestart(reason)
  }
  
}