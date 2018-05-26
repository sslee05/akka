package com.sslee.stream.flow

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import akka.testkit.TestKit
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem

class JsonEitherTest extends TestKit(ActorSystem("test")) with WordSpecLike with MustMatchers with SprayJsonSupport with DefaultJsonProtocol{
  
  object Data {

    case class Success[A](result: A)

    object Success {

      implicit def successFormat[A](implicit format: JsonFormat[A]) = new RootJsonFormat[Success[A]] {

        override def write(value: Success[A]): JsValue = {
          JsObject("ok" -> JsBoolean(true), "result" -> format.write(value.result))
        }

        override def read(json: JsValue): Success[A] = {
          val root = json.asJsObject
          (root.fields.get("ok"), root.fields.get("result")) match {
            case (Some(JsTrue), Some(jsValue)) => 
              deserializationError("JSON TEst")
              //Success(format.read(jsValue))

            case _ => deserializationError("JSON not a Success")
          }
        }
      }

    }

    case class Failure(reason: String)

    object Failure {

      implicit object failureFormat extends RootJsonFormat[Failure] {

        override def write(value: Failure): JsValue = {
          JsObject("ok" -> JsBoolean(false), "error" -> JsString(value.reason))
        }

        override def read(json: JsValue): Failure = {
          val root = json.asJsObject
          (root.fields.get("ok"), root.fields.get("error")) match {
            case (Some(JsFalse), Some(JsString(reason))) => Failure(reason)

            case _ => deserializationError("JSON not a Failure")
          }
        }
      }
    }

    type Result[A] = Either[Failure, Success[A]]

    implicit def rootEitherFormat[A : RootJsonFormat, B : RootJsonFormat] = new RootJsonFormat[Either[A, B]] {
      val format = DefaultJsonProtocol.eitherFormat[A, B]

      def write(either: Either[A, B]) = format.write(either)

      def read(value: JsValue) = format.read(value)
    }

  }

  "Example Unmarshalling JSON Response as Either[Failure, Success]" must {

    import Data._

    "unmarshalling a Success[String] json response" in {
      val result: Result[String] =  Right(Success("Success!"))
      val json = Success("CadetBlue").toJson
      
      println(s"###########${json.convertTo[Either[Failure,Success[String]]]}")
      
      
      //val jsonResponse = HttpResponse(status = StatusCodes.OK, entity = marshalUnsafe(result))
      //jsonResponse.as[Result[String]] must beRight(Right(Success("Success!")))
    }

    "unmarshalling a Failure json response" in {
      val result: Result[String] = Left(Failure("Failure!"))
      //val jsonResponse = HttpResponse(status = StatusCodes.OK, entity = marshalUnsafe(result))
      //jsonResponse.as[Result[String]] must beRight(Left(Failure("Failure!")))
    }
  }
}