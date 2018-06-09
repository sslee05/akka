package com.sslee.cluster.wordcount

import akka.actor.ActorLogging
import akka.actor.Actor
import scala.concurrent.duration._
import com.sslee.cluster.messages._
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.ReceiveTimeout
import akka.actor.Terminated
import akka.actor.Props

class JobMaster extends Actor with ActorLogging with CreateWorkerRouter {
  
  import context._
  
  val router = createWorkerRouter //workers
  var workers = Set[ActorRef]()
  var textParts = Vector[List[String]]() //처리할 모든 text들
  var intermediateResult = Vector[Map[String,Int]]() //작업자들이 작업한 내용으로 아직 총 합을 하지 않는 상태
  var workGiven = 0//작업을 준 작업자의 수  
  var workReceived = 0 //작업자에게 작업 완료를 받은 수
  
  def receive = idle
  
  def idle: Receive = {
    case StartJob(jobName, text) =>
      log.debug(s"##### jobMaster receive message $jobName $text ")
      textParts = text.grouped(10).toVector
      //schedule(init, interval, receiver, message)
      //init=0 은 즉시, interval=1000 1초 간격으로, receiver=router에게 message=Work 를 보낸다.  
      val cancellable = system.scheduler.schedule(0 millis, 1000 millis, router, Work(jobName,self))
      log.debug(s"##### router path is $router")
      //router ! Work(jobName,self)
      become(working(jobName,sender,cancellable))
      //become(working(jobName,sender))
  }
  
  def working(jobName: String, receptionist: ActorRef, cancellable: Cancellable) : Receive = {
  //def working(jobName: String, receptionist: ActorRef) : Receive = {
    
    //작업자가 일할 준비가 되었음을 알림
    case Enlist(worker) => 
      watch(worker)
      workers = workers + worker
     
    //작업자가 새작업을 요청을 알림
    case NextTask =>
      if(textParts.isEmpty)
        sender() ! WorkLoadDepleted
      else {
        sender() ! Task(textParts.head, self)
        textParts = textParts.tail
        workGiven += 1
      }
     
    //작업자가 할당 받은 작업을 다했다고 알림
    case TaskResult(countMap) =>
      log.info(s"#####master TaskResult($countMap)")
      intermediateResult = intermediateResult :+ countMap
      workReceived += 1
      
      if(textParts.isEmpty && workGiven == workReceived) {
        log.info(s"#####master intermediateResult($intermediateResult)")
        //cancellable.cancel()//취소가 성공적이면 true, 이미 취소되었다면 false
        become(finishing(jobName, receptionist, workers))
        setReceiveTimeout(Duration.Undefined)
        self ! MergeResults
      }
      
    case ReceiveTimeout =>
      if(workers.isEmpty) {
        log.info(s"#####works no works responsed in time. canceling job $jobName ")
        stop(self)
      }
      else setReceiveTimeout(Duration.Undefined)
      
    case Terminated(worker) =>
      log.info(s"#####work $worker got terminated. canceling job $jobName")
      stop(self)
     
  }
  
  def finishing(jobName: String, receptionist: ActorRef, workers: Set[ActorRef]): Receive = {
    case MergeResults => 
      log.info(s"#####merges $merges")
      val merge = merges
      workers.foreach(stop(_))
      receptionist ! WordCount(jobName, merge)
      
    case Terminated(worker) =>
      log.info(s"#####job is $jobName is finishing. Worker ${worker.path.name} is stopped.")
  }
  
  def merges: Map[String, Int] = intermediateResult.reduce { (b,a) => 
    (b.keys ++ a.keys).foldLeft(Map.empty[String,Int])((ma,key) => ma.updated(key, b.getOrElse(key, 0) + a.getOrElse(key, 0)))
  }
}