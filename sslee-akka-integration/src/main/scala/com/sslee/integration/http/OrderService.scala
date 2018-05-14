package com.sslee.integration.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import com.sslee.integration.messages._
import com.sslee.integration.http._
import scala.concurrent.ExecutionContext
import akka.util.Timeout
import akka.http.scaladsl.model.StatusCodes
import scala.xml.{NodeSeq,XML}

trait OrderService {
  
  implicit def timeout: Timeout
  implicit def executionContext: ExecutionContext
  
  //서비스를 처리할 Actor
  //이를 추상으로 하게 함으로써 
  //remote proxy ActorRef, local ActorRef, testProbe 과 
  //route와의 관심사를 분리 시킨다.
  val orderProcessor: ActorRef
  
  def routes = getOrder ~ postOrders
  
  def getOrder = get {
    pathPrefix("orders" / IntNumber) { id =>
      //RouteDirectives def onSuccess(magnet: OnSuccessMagnet)
      //object OnSuccessMagnet {
      //   implicit def apply[T](future: ⇒ Future[T])(implicit tupler: Tupler[T])
      onSuccess(orderProcessor ask TrackingId(id) ) {
        case result: TrackingOrder => 
          //RouteDirectives.complete(m: ⇒ ToResponseMarshallable): StandardRoute
          complete(
            //import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
            <statusResponse>
							<id>{result.orderId}</id>
							<status>{result.status}</status>
					  </statusResponse>    
          )
          
        case result: NoSuchOrder =>
          complete(StatusCodes.NotFound)
      }
    }
  }
  
  def postOrders = post {
    path("orders") {
      entity(as[NodeSeq]) { xml =>
        val order = toOrder(xml)
        onSuccess(orderProcessor ask order) {
          case result: TrackingOrder => 
            complete(
              <confirm>
						    <id>{result.orderId}</id>
								<status>{result.status}</status>
						 </confirm>    
            )
            
          case result => complete(StatusCodes.BadRequest)
        }
      }
    }
  }
  
  def toOrder(xml: NodeSeq): Order = {
    println(s"######### $xml")
    val order = xml \\ "order"
    val customerId = (order \\ "customerId").text
    val productId = (order \\ "productId").text
    val number = (order \\ "number").text.toInt
    
    Order(customerId, productId, number)
  }
  
}