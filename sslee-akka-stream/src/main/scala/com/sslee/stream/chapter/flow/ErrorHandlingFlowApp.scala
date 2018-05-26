package com.sslee.stream.chapter.flow

import akka.stream.scaladsl._
import spray.json._
import com.sslee.stream.resources._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import akka.util.ByteString
import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure
import com.sslee.stream.Event2Marshalling


object ErrorHandlingFlowApp extends App with Event2Marshalling {
  
  implicit val system = ActorSystem("ActorSystemWithErrorHandling")
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  val jsonStr = 
    """
      [
      {
        "host": "my-host-1",
        "service": "web-app",
        "state": "ok",
        "time": "2015-08-12T12:12:00.127Z",
        "description": "5 tickets sold to RHCP."
      },
      {
        "host": "my-host-2",
        "service": "web-app",
        "state": "ok",
        "time": "2015-08-12T12:12:01.127Z",
        "description": "3 tickets sold to RHCP."
      },
      {
        "host": "my-host-3",
        "service": "web-app",
        "state": "ok",
        "time": "2015-08-12T12:12:02.127Z",
        ":",
        "description": "1 tickets sold to RHCP."
      },
      {
        "host": "my-host-3",
        "service": "web-app",
        "state": "error",
        "time": "2015-08-12T12:12:03.127Z",
        "description": "exception occurred..."
      }
      """
  
  val path = Files.createTempFile("errorHandling", "json")
  val jsonByte = jsonStr.getBytes("UTF-8")
  Files.write(path, jsonByte, StandardOpenOption.APPEND)
  
  
  val source = FileIO.fromPath(path)
    .via(JsonFraming.objectScanner(1024 * 1024))
    .via(Flow[ByteString].map(b => b.decodeString("UTF-8")))
    .via(Flow[String].map{s =>
      try{ Right(s.parseJson.convertTo[Event2]) } catch { case e: Exception => Left(e.getMessage)}
     })
    
  val runGraph: RunnableGraph[Future[Seq[Either[String,Event2]]]] = 
    source.toMat(Sink.seq[Either[String,Event2]])(Keep.right)
    
  val result = runGraph.run()
  result.onComplete {
    case Success(rs) => 
      rs.map {
        case Right(event) => println(s"##### Right => $event")
        case Left(error)  => println(s"##### Left => $error")
        case o => println(s"##### Other => o")
      }
      system.terminate()
    case Failure(e) => 
      println(s"##### Failure => ${e}")
      system.terminate()
  }
        
}