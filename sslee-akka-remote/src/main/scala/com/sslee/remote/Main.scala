package com.sslee.remote

import com.typesafe.config.Config
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import scala.util.Failure
import akka.event.Logging
import scala.util.Success
import akka.http.scaladsl.Http.ServerBinding

object Main extends App with Startup {
  
  val config = ConfigFactory.load("singlenode")
  implicit val system = ActorSystem("SingleNode", config)
  
  val api = new RestApi() {
    implicit val requestTimeout = configuredRequestTimeout(config)
    implicit def executeContext = system.dispatcher
  
    def createBoxOffice() = system.actorOf(BoxOffice.props, BoxOffice.name)
  }
  
  startup(api.routes)
  
}
