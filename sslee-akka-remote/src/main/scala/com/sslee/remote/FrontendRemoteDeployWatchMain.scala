package com.sslee.remote

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import akka.util.Timeout
import akka.actor.ActorRef
import akka.event.Logging

object FrontendRemoteDeployWatchMain extends App with Startup {
  
  val config = ConfigFactory.load("frontend-remote-deploy")
  implicit val system = ActorSystem("frontend-remote-deploy-watch",config)
  
  val api = new RestApi {
    implicit val executeContext: ExecutionContextExecutor = system.dispatcher
    implicit val requestTimeout: Timeout = configuredRequestTimeout(config)
    
    // 이부분이 local, 원격(path방식), 원격(deploy방식)에 따라 달라지는 부분
    //local 인경우 직접 BoxOffice를, 원격지는 Proxy를 생성
    def createBoxOffice(): ActorRef = {
      system.actorOf(RemoteBoxOfficeForwarder.props,RemoteBoxOfficeForwarder.name) 
    }
  }
  
  startup(api.routes)
}