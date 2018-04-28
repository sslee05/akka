package com.sslee.structure.routingslip

import akka.actor.Actor
import akka.actor.ActorLogging

class PaintCar(color: String) extends Actor with ActorLogging with RouteSlip {
  
  def receive = {
    case RouteSlipMessage(routSlip, car: Car) =>
      log.debug(s"#####$self receive message $RouteSlipMessage")
      sendMessageToNextTask(routSlip, car.copy(color = color)) 
  }
}