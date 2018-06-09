package com.sslee.cluster.router

package object messages {
  
  final case class StatesJob(text: String)
  final case class StateResult(meanWordLength: Double)
  final case class JobFail(reason: String)
  
}