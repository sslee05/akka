package com.sslee.stream

import com.sslee.stream.resources._
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import spray.json._

trait Event2Marshalling extends DefaultJsonProtocol {
  
  implicit val dateTimeFormat = new JsonFormat[Either[String,ZonedDateTime]] {
    
    
    def write(dateTime: Either[String,ZonedDateTime]): JsString = 
      JsString(dateTime.getOrElse(ZonedDateTime.now()).format(DateTimeFormatter.ISO_INSTANT))
    
    def read(jsValue: JsValue): Either[String,ZonedDateTime] = jsValue match {
      case JsString(str) => 
        try {
          Right(ZonedDateTime.parse(str))
        }
        catch {
          case e: Exception => 
            Left(s"Could not deserialize $str to ZonedDateTime")
        }
      case js => 
        Left(s"Could not deserialize $js to ZonedDateTime")
    }
  }
  
  implicit val stateFormat = new JsonFormat[Either[String,State]] {
    
    def write(state: Either[String,State]): JsString = 
      JsString(State.norm(state.getOrElse(Error)))
    
    def read(jsValue: JsValue): Either[String,State] = jsValue match {
      case JsString("ok") => Right(Ok)
      case JsString("warning") => Right(Warning)
      case JsString("error") => Right(Error)
      case JsString("critical") => Right(Critical)
      case js => 
        Left(s"Could not deserialize $js to ZonedDateTime")
    }
  }
  
  
  implicit val parseInfoFormat = jsonFormat1(ParseInfo)
  implicit val event2Format = jsonFormat7(Event2)
  //implicit val event2EitherFormat = DefaultJsonProtocol.eitherFormat[ParseInfo, Event2]
  
  /*
  implicit def serviceSuccessJsonFormat(implicit format: JsonFormat[Event2]) = new RootJsonFormat[Event2] {

    override def write(value: Event2): JsValue = {
      JsObject("ok" -> JsBoolean(true), "result" -> format.write(value))
    }

    override def read(json: JsValue): Event2 = {
      val root = json.asJsObject
      (root.fields.get("ok"), root.fields.get("result")) match {
        case (Some(JsTrue), Some(jsValue)) => format.read(jsValue)

        case _ => throw new DeserializationException("JSON not a ServiceSuccess")
      }
    }
  }

  implicit object errorMessageJsonFormat extends RootJsonFormat[ParseInfo] {

    override def write(value: ParseInfo): JsValue = {
      JsObject("ok" -> JsBoolean(false), "error" -> JsString(value.msg))
    }

    override def read(json: JsValue): ParseInfo = {
      try {
        println(s"######call $json ")
        val root = json.asJsObject
        println(s"######root $root ")
        
        (root.fields.get("ok"), root.fields.get("error")) match {
          case (Some(JsFalse), Some(JsString(errorText))) => ParseInfo(errorText)
  
          case (a,b) => 
            println(s"######a $a b: $b ")
            throw new DeserializationException("JSON not a ErrorMessage")
        }
        
      } catch {
        case e: DeserializationException => 
          //ParseInfo(e.toString())
          println(s"######EXXXXXXXXXX=> $e ")
          //ParseInfo(e.toString())
          //throw new DeserializationException("JSON not a ErrorMessage")
          throw e
        case e: Exception =>
          println(s"######EEEEEEEEEEE=> $e ")
          ParseInfo(e.toString())
      }
      
    }
  }


  implicit def rootEitherFormat[A : RootJsonFormat, B : RootJsonFormat] = new RootJsonFormat[Either[A, B]] {
    val format = DefaultJsonProtocol.eitherFormat[A, B]

    def write(either: Either[A, B]) = format.write(either)

    def read(value: JsValue) = format.read(value)
  }
  
  //implicit val event2Impl = rootEitherFormat(errorMessageJsonFormat,serviceSuccessJsonFormat(event2Format))
  implicit val event2Impl = rootEitherFormat(errorMessageJsonFormat,event2Format)
  */
}