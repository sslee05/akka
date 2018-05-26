package com.sslee.stream.http

import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import java.nio.file.Path
import java.nio.file.StandardOpenOption._
import com.sslee.stream.EventMarshalling
import akka.stream.scaladsl._
import akka.util.ByteString
import scala.concurrent.Future
import akka.stream.IOResult
import com.sslee.stream.resources._
import com.sslee.stream.marshaller.LogEntityMarshaller
import spray.json._
import akka.NotUsed
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.PathMatchers.Segment
import scala.util.Success
import akka.Done
import akka.http.scaladsl.model.StatusCodes
import scala.util.Failure
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import java.nio.file.Files
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.marshalling.{Marshaller,ToEntityMarshaller}
import akka.http.scaladsl.model.HttpEntity





class LogFileNegoApi(val logDir: Path, val maxLine: Int, val maxJsObject: Int)
  (implicit val ec: ExecutionContext, val mat: Materializer) extends EventMarshalling{
  
  def logFile(logId: String): Path = logDir.resolve(logId)
  def logFileSource(logId: String): Source[ByteString, Future[IOResult]] = 
    FileIO.fromPath(logFile(logId), maxLine)
    
  val eventToStringFlow = Flow[Event].map(e => ByteString(e.toJson.compactPrint))
  def fileSink(logId: String) = 
    FileIO.toPath(logFile(logId), Set(CREATE, WRITE, APPEND))

  def routes = postRoute ~ getRoute ~ deleteRoute
  
  
  implicit val eventUnmarshaller: Unmarshaller[HttpEntity, Source[Event,_]] = 
    LogEntityMarshaller.createEventUnMarshaller(maxLine, maxJsObject)
  
  def postRoute = pathPrefix("logs" / Segment) { logId =>
    pathEndOrSingleSlash {
      post {
        //as는 Unmarshaller 암시자가 필요
        entity(as[Source[Event,_]]) { esrc =>
          onComplete(
            esrc.via(eventToStringFlow)
            .toMat(fileSink(logId))(Keep.right)
            .run()    
          ) {
            case Success(IOResult(count, Success(Done))) => 
              complete((StatusCodes.OK, LogReceipt(logId, count)))
            case Success(IOResult(count, Failure(e))) => 
              complete((
                StatusCodes.BadRequest,
                ParseError(logId, e.getMessage)
              ))
            case Failure(e) => 
              complete((
                StatusCodes.BadRequest,
                ParseError(logId, e.getMessage)
              ))
          }
        }
      }
    }
  }
  
  implicit val eventMarshaller: ToEntityMarshaller[Source[ByteString, _]] = 
    LogEntityMarshaller.createEventMarshaller(maxJsObject)
  
  def getRoute = pathPrefix("logs" / Segment) { logId =>
    pathEndOrSingleSlash {
      get {
        extractRequest{ req => 
          if(Files.exists(logFile(logId))) {
            val src = logFileSource(logId)
            //Marshal.toResponseFor 는 ToResponseMarshaller 암시자 를 필요로 한다.
            complete(Marshal(src).toResponseFor(req))
          } else {
            complete(StatusCodes.NotFound)
          }
        }
      }
    }
  }
  
  def deleteRoute = pathPrefix("logs" / Segment) { logId =>
    pathEndOrSingleSlash {
      delete {
        if(Files.deleteIfExists(logFile(logId)))
          complete(StatusCodes.OK)
        else
          complete(StatusCodes.NotFound)
      }
    }
  }
}