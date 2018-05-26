package com.sslee.stream

package object resources {
  
  import java.time.ZonedDateTime
  
  sealed trait State
  case object Critical extends State
  case object Error extends State
  case object Ok extends State
  case object Warning extends State
  
  object State {
    def norm(str: String): String = str.toLowerCase()
    def norm(state: State): String = norm(state.toString)
    
    val ok = norm(Ok)
    val error = norm(Error)
    val critical = norm(Critical)
    val warning = norm(Warning)
    
    def unapply(str: String): Option[State] = {
      val n = norm(str)
      if( n == ok) Some(Ok)
      else if(n == error) Some(Error)
      else if(n == critical)  Some(Critical)
      else if(n == warning) Some(Warning)
      else None
    }
  }
  
  case class LogParseException(msg: String) extends Exception(msg)
  
  case class ParseInfo(msg: String)
  
  case class Event(host: String,
      service: String,
      state: State,
      time: ZonedDateTime,
      description: String,
      tag: Option[String] = None,
      metric: Option[Double] = None)
      
  case class Event2(host: String,
      service: String,
      state: Either[String,State],
      time: Either[String,ZonedDateTime],
      description: String,
      tag: Option[String] = None,
      metric: Option[Double] = None)
      
  case class LogReceipt(logId: String, written: Long)
  case class ParseError(logId: String, msg: String)
}