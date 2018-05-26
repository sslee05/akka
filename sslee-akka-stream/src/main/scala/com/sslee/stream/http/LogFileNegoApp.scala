package com.sslee.stream.http

import com.typesafe.config.ConfigFactory
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.FileSystems
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.Http.ServerBinding
import akka.event.Logging
import scala.util.Success
import scala.util.Failure

object LogFileNegoApp extends App {
  
  val config = ConfigFactory.load()
  val host = config.getString("akka.http.server.host")
  val port = config.getInt("akka.http.server.port")
  
  val logsDir: Path = {
    val dir = config.getString("akka.log-stream-example.dir")
    Files.createDirectories(FileSystems.getDefault.getPath(dir))
  }
  
  val maxLine = config.getInt("akka.log-stream-example.max-line")
  val maxJsObject = config.getInt("akka.log-stream-example.max-js-object")
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  
  val api = new LogFileNegoApi(logsDir, maxLine, maxJsObject).routes
  
  val bindingFuture: Future[ServerBinding] = 
    Http().bindAndHandle(api, host, port)
    
  val log = Logging(system.eventStream, "log")
  
  bindingFuture.onComplete {
    case Success(serverBinding) => 
      log.info(s"Bound to ${serverBinding.localAddress}")
    case Failure(e) => 
      log.info(s"Bound to Fail ${e.getMessage}")
  }
}