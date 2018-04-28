package com.sslee.remote

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import scala.concurrent.ExecutionContextExecutor
import akka.stream.Materializer
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.event.Logging
import scala.util.Success
import scala.util.Failure

trait Startup extends RequestTimeout {
  
  def startup(route: Route)(implicit system: ActorSystem) = {
    val host = system.settings.config.getString("http.host")
    val port = system.settings.config.getInt("http.port")
    
    startHttpServer(route,host,port)
  }
  
  def startHttpServer(route: Route, host: String, port: Int)(implicit system: ActorSystem) = {
    
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val materializer: Materializer = ActorMaterializer()
    
    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(route,host,port)
    
    val log = Logging(system.eventStream, "SSLEE TicketServer")
    bindingFuture.map(serverBnd => 
      log.debug(s"RestApi bound to ${serverBnd.localAddress}")
    ).onComplete{
      case Success(v) => 
      case Failure(e) => 
        log.error(s"fail to bind to {}:{}!",host, port)
        system.terminate()
    }
    
  }
  
}