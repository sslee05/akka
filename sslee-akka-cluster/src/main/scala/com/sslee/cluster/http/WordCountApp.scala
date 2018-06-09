package com.sslee.cluster.http

import com.sslee.cluster.messages._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.actor.Props
import com.sslee.cluster.wordcount.JobReceptionist
import akka.stream.Materializer
import scala.concurrent.ExecutionContextExecutor
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.Http.ServerBinding
import akka.event.Logging
import scala.util.Success
import scala.util.Failure
import akka.cluster.Cluster

object WordCountApp extends App {
  
  val config = ConfigFactory.load("master")
  val nrMaxRetries = config.getInt("nr-max-retries") 
  implicit val actorSystem = ActorSystem("wordcount",config)
  //actorSystem.eventStream.setLogLevel(Logging.DebugLevel)
  
  //Cluster(actorSystem)
  
  val routes = new WordCountApi {
    implicit val system = actorSystem
    implicit val executionContext = system.dispatcher
    implicit def requestTimeout = Timeout(3 seconds)
    def createReceptionist = system.actorOf(Props(new JobReceptionist(nrMaxRetries)))
  }.routes
  
  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  implicit val materializer: Materializer = ActorMaterializer()
    
  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes,host,port)
  val logging = Logging(actorSystem.eventStream,"WordCount App")
  
  bindingFuture.onComplete {
    case Success(v) =>
      logging.debug("completed start WordCount Http server")
    case Failure(e) => 
      logging.error(e, "failed start WordCount Http server")
      actorSystem.terminate()
  }
}