package com.sslee.structure.routingslip

import akka.actor.ActorRef
import akka.actor.ActorLogging

//작업사이에 전달할 message 
//routslip 구조에서는 다음에 전달될 ActorRef 목과 전달할 message가 있어야 한다.
case class RouteSlipMessage(routeSlip: Seq[ActorRef], message: AnyRef)

object CarOption extends Enumeration {
  val CAR_COLOR_GRAY, NAVIGATION, PARKING_SENSORS = Value
}

case class Order(options: Seq[CarOption.Value])
case class Car(color: String = "",
               hasNavigation: Boolean = false,
               hasParkingSensors: Boolean = false)

trait RouteSlip { self : ActorLogging =>  
  
  def sendMessageToNextTask(routeSlip: Seq[ActorRef], message: AnyRef) = {
    
    log.debug(s"#####$self sendMessageToNextTask method ")
    
    val head = routeSlip.head
    val tail = routeSlip.tail
    
    log.debug(s"#####$self call next RoutSlip  head $head tail $tail")
    if(tail.isEmpty)
      head ! message
    else
      head ! RouteSlipMessage(tail, message)
    
  }
}
