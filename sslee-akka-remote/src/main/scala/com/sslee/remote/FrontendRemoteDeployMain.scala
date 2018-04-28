package com.sslee.remote

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.event.Logging
import scala.concurrent.ExecutionContextExecutor
import akka.util.Timeout
import akka.actor.ActorRef

object FrontendRemoteDeployMain extends App with Startup {
  
  val config = ConfigFactory.load("frontend-remote-deploy")
  implicit val system = ActorSystem("FrontendRemoteDeployMode",config)
  
  val api = new RestApi() {
    val log = Logging(system.eventStream,"FrontendRemoteDeployMode")
    
    implicit val executeContext: ExecutionContextExecutor = system.dispatcher
    implicit val requestTimeout: Timeout = configuredRequestTimeout(config)
    
    def createBoxOffice: ActorRef = {
      system.actorOf(BoxOffice.props,BoxOffice.name)
    }
  }
  
  startup(api.routes)
}