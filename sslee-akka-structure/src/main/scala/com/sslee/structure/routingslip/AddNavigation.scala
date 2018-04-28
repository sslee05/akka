package com.sslee.structure.routingslip

import akka.actor.Actor
import akka.actor.ActorLogging

class AddNavigation extends Actor with ActorLogging with RouteSlip {
  
  def receive = {
    case RouteSlipMessage(routeSlip,car: Car) =>
      log.debug(s"#####$self receive message $RouteSlipMessage")
      sendMessageToNextTask(routeSlip, car.copy(hasNavigation = true))
  }
}