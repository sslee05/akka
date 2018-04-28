package com.sslee.structure.routingslip

import akka.actor.ActorLogging
import akka.actor.Actor

class AddParkingSensors extends Actor with ActorLogging with RouteSlip {
  
  def receive = {
    case RouteSlipMessage(routeSlip, car: Car) => 
      log.debug(s"$self received message $RouteSlipMessage")
      sendMessageToNextTask(routeSlip, car.copy(hasParkingSensors = true))
  }
}