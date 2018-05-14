package com.sslee.integration.http

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import akka.util.Timeout
import akka.actor.ActorSystem

class OrderServiceApi(val orderProcessor:ActorRef)
  (implicit val timeout: Timeout, implicit val system: ActorSystem) extends OrderService {
  
  //implicit val to = timeout
  implicit def executionContext = system.dispatcher
  
}