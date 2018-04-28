package com.sslee.remote

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.ExecutionContextExecutor
import akka.event.Logging

object FrontendNodeMain extends App with Startup {
  
  val config = ConfigFactory.load("frontend")
  implicit val system = ActorSystem("frontend", config)
  
  val api = new RestApi() {
    
    val log = Logging(system.eventStream,"RestApi application")
    
    implicit val executeContext: ExecutionContextExecutor = system.dispatcher
    implicit val requestTimeout: Timeout = configuredRequestTimeout(config)
    
    // 이부분이 local, 원격(path방식), 원격(deploy방식)에 따라 달라지는 부분
    //local 인경우 직접 BoxOffice를, 원격지는 Proxy를 생성
    def createPath(): String = {
      val backendConfig = ConfigFactory.load("frontend").getConfig("backend")
      val host = backendConfig.getString("host")
      val port = backendConfig.getInt("port")
      val protocol = backendConfig.getString("protocol")
      val systemNode = backendConfig.getString("system")
      val actorName = backendConfig.getString("actor")
      log.debug(s"####### path =>$protocol://$systemNode@$host:$port/$actorName")
      s"$protocol://$systemNode@$host:$port/$actorName"
    }
    
    def createBoxOffice: ActorRef = {
      val path = createPath
      system.actorOf(Props(new RemoteLookupProxy(path)),"lookupBoxOffice")
    }
  }
  
  startup(api.routes)
  
}