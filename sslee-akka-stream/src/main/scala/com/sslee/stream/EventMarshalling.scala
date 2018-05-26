package com.sslee.stream

import spray.json._
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.sslee.stream.resources._

trait EventMarshalling extends DefaultJsonProtocol {
  
  implicit val dateTimeFormat = new JsonFormat[ZonedDateTime] {
    
    def write(dateTime: ZonedDateTime): JsString = JsString(dateTime.format(DateTimeFormatter.ISO_INSTANT))
    
    def read(jsValue: JsValue): ZonedDateTime = jsValue match {
      case JsString(str) => 
        try {
          ZonedDateTime.parse(str)
        }
        catch {
          case e: Exception => 
            val msg = s"Could not deserialize $str to ZonedDateTime"
            deserializationError(msg)
        }
      case js => 
        val msg = s"Could not deserialize $js to ZonedDateTime"
            deserializationError(msg)
    }
  }
  
  implicit val stateFormat = new JsonFormat[State] {
    
    def write(state: State): JsString = JsString(State.norm(state))
    
    def read(jsValue: JsValue): State = jsValue match {
      case JsString("ok") => Ok
      case JsString("warning") => Warning
      case JsString("error") => Error
      case JsString("critical") => Critical
      case js => 
        val msg = s"Could not deserialize $js to ZonedDateTime"
        deserializationError(msg)
    }
  }
  
  implicit val eventFormat = jsonFormat7(Event)
  implicit val eventEitherFormat = DefaultJsonProtocol.eitherFormat[String,Event]
  implicit val logReceiptFormat = jsonFormat2(LogReceipt)
  implicit val parseErrorFormat = jsonFormat2(ParseError)
  
}