package com.sslee.integration.http

import scala.concurrent.duration._
import com.typesafe.config.Config
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.event.Logging
import scala.concurrent.Future
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http

object OrderServiceBoot extends App with RequestTimeout {
  
  val config = ConfigFactory.load()
  val host = config getString "akka.http.server.host"
  val port = config getInt "akka.http.server.port"
  
  println(s"########################host=>$host")
  println(s"########################host=>$port")
  
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val timeout = requestTimeout(config)
  
  val orderProcessor = system.actorOf(Props(new OrderProcessor))
  val routes = (new OrderServiceApi(orderProcessor)).routes
  
  implicit val materializer = ActorMaterializer()
  val log = Logging(system.eventStream, "orderService Http server")
  
  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes,host,port) 
}

trait RequestTimeout {
  def requestTimeout(config: Config): Timeout = {
    val requestTimeout = config.getString("akka.http.server.request-timeout")
    println(s"########################requestTimeout=>$requestTimeout:")
    val duration = Duration(requestTimeout)
    
    FiniteDuration(duration.length, duration.unit)
  }
}