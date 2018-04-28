package com.sslee.chapter01

import akka.http.scaladsl.server.Directive._
import com.typesafe.config.{Config,ConfigFactory}
import akka.util.Timeout
import akka.event.Logging
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.Http.ServerBinding
import scala.util.{Failure,Success}

object Main extends App with RequestTimeout {
  
  val config = ConfigFactory.load()
  val host = config.getString("http.host")//설정으로 부터 가져옴 
  val port = config.getInt("http.port")//설정으로 부터 가져옴
  
  implicit val system = ActorSystem()
  implicit val ec:ExecutionContextExecutor = system.dispatcher //bindHandle가 비동기 때문에  필요함.
  
  val api = new RestApi(system,requestTimeout(config)).routes
  
  implicit val materializer = ActorMaterializer()
  val bindFuture: Future[ServerBinding] = 
    Http().bindAndHandle(api,host,port) // RestApi 의 라우트 로 server를 시작한다.
    
  val log = Logging(system.eventStream,"sslee-start akka application")
  bindFuture.map(serverBinding => log.info(s"RestApi bound to ${serverBinding.localAddress}")).onComplete {
      case Success(v) => 
      case Failure(ex) => 
        log.error(ex,s"Failed to bind to {}:{}!",host,port)
        system.terminate
      
  }
  
  
}

trait RequestTimeout {
  import scala.concurrent.duration._
  
  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    println(s"akka.http.server.request-timeout=>${t}")
    val d = Duration(t)
    FiniteDuration(d.length,d.unit)
  }
}