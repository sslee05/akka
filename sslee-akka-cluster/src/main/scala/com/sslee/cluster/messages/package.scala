package com.sslee.cluster

package object messages {
  
  import akka.actor.ActorRef
  
  case class Work(jobName: String, order: ActorRef) // 작업
  case object NextTask//다음 작업 요청 
  case class Enlist(worker: ActorRef)//worker 등록 요청 
  case class Task(words: List[String], order: ActorRef)//worker가 할일 
  case class TaskResult(result: Map[String,Int])//worker 작업 결과
  case class StartJob(jobName: String, words: List[String])//
  case object WorkLoadDepleted
  case object MergeResults //works에게 받은 중간 작업의 결과를 merge
  case class WordCount(jobName: String, wordNrResult: Map[String,Int])// 최종 결과 값

  sealed trait JobResponse
  case class JobSucess(name: String, result: Map[String,Int]) extends JobResponse
  case class JobFailure(name: String) extends JobResponse
  case class JobRequest(name: String, text: List[String])
  case class Job(name: String, text: List[String], responseTo: ActorRef, master: ActorRef)
}