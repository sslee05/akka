package com.sslee.integration.consumer

import akka.camel.Consumer
import akka.actor.ActorRef
import akka.camel.CamelMessage
import scala.xml.XML
import com.sslee.integration.messages._
import akka.actor.ActorLogging

class ConsumerEndPoint(uri: String, processor: ActorRef) extends Consumer with ActorLogging {
  
  def endpointUri = uri
  
  def receive = {
    case msg: CamelMessage => {
      val content = msg.bodyAs[String]
      val xml = XML.loadString(content)
      val order = xml \\ "order"
      val customer = (order \\ "customerId").text
      val productId = (order \\ "productId").text
      val number = (order \\ "number").text.toInt
      
      processor ! Order(customer, productId, number)
    }
    case msg =>
      log.debug(s"######## fileConsumerEndPoint receive other message")
  }
}