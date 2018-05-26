package com.sslee.stream.http

import com.typesafe.config.ConfigFactory
import java.nio.file.Files
import java.nio.file.FileSystems
import akka.actor.ActorSystem
import akka.stream.Supervision
import com.sslee.stream.resources._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import java.nio.file.Path
import scala.concurrent.Future
import akka.event.Logging
import scala.util.Failure
import scala.util.Success

object LogFileApp extends App {
  
  val config = ConfigFactory.load()
  val host = config.getString("akka.http.server.host")
  val port = config.getInt("akka.http.server.port")
  
  val logsDir: Path = {
    val dir = config.getString("akka.log-stream-example.dir")
    Files.createDirectories(FileSystems.getDefault.getPath(dir))
  }
  
  val maxLine = config.getInt("akka.log-stream-example.max-line")
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  
  implicit val mat = ActorMaterializer()
 
  val api = new LogFileApi(logsDir,maxLine).routes
 
  val bindingFuture: Future[ServerBinding] = 
    Http().bindAndHandle(api, host, port)
   
  val log = Logging(system.eventStream, "logs")
  bindingFuture.onComplete {
    case Success(serverBinding) => 
      log.info(s"Bound to ${serverBinding.localAddress}")
    case Failure(e) =>
      log.info(s"Bound to Fail ${e.getMessage}")
  }
}