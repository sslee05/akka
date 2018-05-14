package com.sslee.integration.http

import org.scalatest.MustMatchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.WordSpecLike
import scala.concurrent.duration._
import akka.actor.Props
import akka.http.scaladsl.model.StatusCodes
import scala.xml.NodeSeq
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

class OrderServiceTest extends WordSpecLike 
  with MustMatchers with OrderService with ScalatestRouteTest {
  
  implicit val executionContext = system.dispatcher
  implicit val timeout = akka.util.Timeout(3 seconds)
  
  val orderProcessor = system.actorOf(Props(new OrderProcessor))
  
  "The order processor" must {
    "return not found if trackingId not found" in {
      Get("/orders/1") ~> routes ~> check {
        status mustEqual StatusCodes.NotFound
      }
    }
    
    "return tracking id for order that was post" in {
      val xml = 
        <order>
					<customerId>sslee</customerId>
          <productId>akka in action</productId>
          <number>10</number>
				</order>
        
      Post("/orders",xml) ~> routes ~> check {
        status mustEqual StatusCodes.OK
        
        val responseXml = responseAs[NodeSeq]
        val id = (responseXml \\ "id").text.toInt
        val orderStatus = (responseXml \\ "status").text
        id mustEqual 1 
        orderStatus mustEqual "created"
      }
      
      Get("/orders/1") ~> routes ~> check {
        status mustEqual StatusCodes.OK
        
        val responseXml = responseAs[NodeSeq]
        val id = (responseXml \\ "id").text.toInt
        val responseStatus = (responseXml \\ "status").text
        
        id mustEqual 1
        responseStatus mustEqual "processing"
      }
    }
  }
  
}