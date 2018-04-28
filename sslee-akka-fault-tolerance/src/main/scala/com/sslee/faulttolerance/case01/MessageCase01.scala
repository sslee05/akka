package com.sslee.faulttolerance.case01

object MessageCase01 {
  
  case class Message(message: String)
  case object StopMessage
  case object InterruptMessage
  
  class ForceRestartException extends IllegalStateException("force restart")
}