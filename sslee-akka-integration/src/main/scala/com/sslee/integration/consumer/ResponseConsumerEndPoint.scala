package com.sslee.integration.consumer

import akka.camel.Consumer
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.camel.CamelMessage
import scala.xml.XML
import com.sslee.integration.messages._

class ResponseConsumerEndPoint(uri: String, processor: ActorRef) extends Consumer with ActorLogging {
  
  def endpointUri = uri
  
  def receive = {
    case msg: CamelMessage => {
      try {
        log.debug(s"##### ReponseConsumerEndPoint receive message $msg")
        val content = msg.bodyAs[String]
        val xml  = XML.loadString(content)
        val order = xml \\ "order"
        val customer = (order \\ "customerId").text
        val productId = (order \\ "productId").text
        val number = (order \\ "number").text.toInt
        
        processor ! Order(customer, productId, number)
        sender() ! <confirm>OK</confirm>
      }
      catch {
        case ex: Exception =>
          sender() ! s"<confirm>${ex.getMessage}</conform>"
      }
    }
  }
  
}