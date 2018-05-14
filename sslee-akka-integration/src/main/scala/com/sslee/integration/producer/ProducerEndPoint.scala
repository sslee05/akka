package com.sslee.integration.producer

import akka.camel.Producer
import akka.actor.ActorLogging
import com.sslee.integration.messages._
import scala.xml.XML
import akka.camel.CamelMessage

class ProducerEndPoint(uri: String) extends Producer with ActorLogging {
  
  def endpointUri = uri
  //요청에 대한 응답을 기다리겠다는 설정 , 응답을 기다리지 않는다면 true설정 true시 이는 기다리기 위한 resource를 사용하지 않는다.
  override def oneway = false  
  
  //전송시 message 변환 작업이 필요시 override한다.
  //이는 message전송 직전에 호출 된다.
  override protected def transformOutgoingMessage(message: Any): Any = message match {
    case msg: Order => {
      val xml = 
        <order>
          <customerId>{msg.customerId}</customerId>
          <productId>{msg.productId}</productId>
          <number>{msg.number}</number>
        </order>
          
       xml.toString().replace("\n", "")
    }
    
    case other => log.debug(s"##### not supported message typ. message is $other")
  }
  
  //요청에 요청했던 송신자에게 전송하기 전에 호출된다.
  //이를 통해 응답을 Application안에서 사용하는 type으로 변환 한다.
  override def transformResponse(message: Any): Any = message match {
    case msg: CamelMessage => 
      try {
        log.debug(s"##### ProducerEndPoint receive message $msg")
        val content = msg.bodyAs[String]
        val xml = XML.loadString(content)
        val res = (xml \\ "confirm").text
        res
      }
      catch {
        case e: Exception =>
          s"TransformException ${e.getMessage}"
      }
  }
  
}