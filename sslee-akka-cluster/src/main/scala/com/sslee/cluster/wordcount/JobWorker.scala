package com.sslee.cluster.wordcount

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.cluster.messages._
import akka.actor.ActorRef
import akka.actor.ReceiveTimeout
import akka.actor.Terminated
import scala.concurrent.duration._

class JobWorker extends Actor with ActorLogging {
  
  import context._
  
  def receive = idle
  
  def idle: Receive = {
    case Work(jobName, master) =>
      log.debug(s"##### worker receive message $jobName $master")
      become(enlisted(jobName, master))
      
      log.info(s"Enlisted, will start working for job $jobName")
      master ! Enlist(self)
      master ! NextTask
      
      watch(master)
      setReceiveTimeout(3 seconds)
      
  }
  
  def enlisted(jobName: String, master: ActorRef): Receive = {
    
    case ReceiveTimeout =>
      master ! NextTask
      
    case Terminated(_) =>
      setReceiveTimeout(Duration.Undefined)
      log.error(s"Master terminated that ran job ${jobName}, stop self")
      
    case Task(words, master) =>
      log.info("process work")
      val result = processTask(words)
      master ! TaskResult(result)
      
    //더이상 작업할 것이 없을때 
    case WorkLoadDepleted =>
      log.info(s"work load job depleted, retiring...")
      setReceiveTimeout(Duration.Undefined)
      become(retired(jobName))
      
  }
  
  def retired(jobName: String): Receive = {
    case Terminated(_) => 
      log.info(s"Master terminated that ran job $jobName, stoping self")
      stop(self)
    case _ => log.error(s"I'm retired")
  }
  
  def processTask(xs: List[String]): Map[String,Int] = { 
    log.info(s"#####processTasking") 
    xs.flatMap(s => s.split("\\W+")).foldLeft(Map.empty[String,Int])((m,a) => m.updated(a, m.getOrElse(a, 0) + 1))
  }
}