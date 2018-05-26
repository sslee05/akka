package com.sslee.stream.fan

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import akka.stream.Materializer
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.Http
import akka.event.Logging
import scala.concurrent.Future
import akka.http.scaladsl.Http.ServerBinding
import scala.util.Failure
import scala.util.Success

object MergeGraphStageApp extends App {
  
  implicit val system = ActorSystem("mergeFanExample")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()
  
  val api = (new MergeGraphApi).route
  println(s"########## api=> $api")
  
  val config = ConfigFactory.load()
  val host = config.getString("akka.http.server.host")
  val port = config.getInt("akka.http.server.port")
  
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