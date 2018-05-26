package com.sslee.stream.fan

import com.sslee.stream.EventMarshalling
import akka.http.scaladsl._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import spray.json._
import com.sslee.stream.EventMarshalling
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshalling.{Marshaller,ToEntityMarshaller}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes

class MergeGraphApi(implicit ec: ExecutionContext, mat: Materializer)  extends MergeGraphStage with EventMarshalling {
  
  implicit val tt: ExecutionContext = ec
  
  def route = logFilesRoute ~ test
  
  def logFilesRoute = pathPrefix("logs" / Segment ) { logId =>
    pathEndOrSingleSlash {
      get {
        //extractRequest { req =>
          complete(
            HttpEntity(ContentTypes.`application/octet-stream`,mergeNotOk(logId))
          )
        //}
      }
    }
  }
  
  def test = pathPrefix("logs2" / Segment ) { logId =>
    pathEndOrSingleSlash {
      get {
        //extractRequest { req =>
          complete(
            HttpEntity(ContentTypes.`text/plain(UTF-8)`,"OK")
          )
        //}
      }
    }
  }
  
}