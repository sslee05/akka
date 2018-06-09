package com.sslee.cluster.wordcountapp

package object messages {
  
  import akka.actor.ActorRef  
  
  case class TaskData(words: String)
  case object NewTask
  case object EndData
  case object ReadyForProcess
  case object ReadyForWorker
  case object ReadyCompleted
  case class StartCount(path: String, fileName: String)
  case class WordCount(data: Map[String,Int])
  case class FailCountWords(msg: String)
  
  trait ResponseForActor
  case object ResponseSuccess extends ResponseForActor
  case class ResponseFail(msg: String) extends ResponseForActor
}