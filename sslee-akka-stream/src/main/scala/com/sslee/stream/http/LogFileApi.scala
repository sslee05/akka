package com.sslee.stream.http

import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.StandardOpenOption._
import akka.stream.scaladsl._
import akka.stream.Materializer
import com.sslee.stream.EventMarshalling
import com.sslee.stream.resources._
import java.util.concurrent.ExecutorService
import akka.NotUsed
import akka.util.ByteString
import spray.json._
import scala.concurrent.Future
import akka.stream.IOResult
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.model.HttpEntity
import scala.util.Success
import akka.Done
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import scala.util.Failure
import akka.http.scaladsl.server._
import java.nio.file.Files
import akka.http.scaladsl.model.ContentTypes
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshalling.Marshal


class LogFileApi(val logDir: Path, val maxLine: Int)(implicit val ec: ExecutionContext, val mat: Materializer) 
  extends EventMarshalling {
  
  def logFile(logId: String): Path = logDir.resolve(logId)
  def logFileSource(logId: String): Source[ByteString, Future[IOResult]] = FileIO.fromPath(logFile(logId), maxLine)
  
  val inFlow: Flow[ByteString, Event, NotUsed] = JsonFraming.objectScanner(maxLine)
    .map(b => b.decodeString("UTF-8"))
    .map(s => s.parseJson.convertTo[Event])
    
  val inFlowLinear: Flow[ByteString, Event, NotUsed] = Framing.delimiter(ByteString("\n"), maxLine)
    .map(b => b.decodeString("UTF-8"))
    .map(s => s.parseJson.convertTo[Event])
  
  val outFlow: Flow[Event, ByteString, NotUsed] = Flow[Event].map(e => ByteString(e.toJson.compactPrint))
  
  val bidiFlow: BidiFlow[ByteString, Event, Event, ByteString, NotUsed] = BidiFlow.fromFlows(inFlow, outFlow)
  val flow: Flow[ByteString, ByteString, NotUsed] = bidiFlow.join(Flow[Event])// 흐름 turning 형은 Event -> Event
  
  def getSinkLogFile(id: String): Sink[ByteString, Future[IOResult]] = 
    FileIO.toPath(logFile(id), Set(CREATE,WRITE,APPEND))
    
  
  def routes = postRoute ~ getRoute
  
  def postRoute = pathPrefix("logs" / Segment) { logId =>
    pathEndOrSingleSlash { 
      post {
        entity(as[HttpEntity]) { entity =>
          onComplete(
            entity.dataBytes
              .via(flow)
              .toMat(getSinkLogFile(logId))(Keep.right)
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
  
  def getRoute = pathPrefix("logs" / Segment) { logId =>
    pathEndOrSingleSlash {
      get {
        if(Files.exists(logFile(logId))) {
          val src: Source[ByteString, Future[IOResult]] = logFileSource(logId)
          complete(
            HttpEntity(ContentTypes.`application/json`,src)    
          )
        }
        else {
          complete(StatusCodes.NotFound)
        }
      }
    }
  }
  
}