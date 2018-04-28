package com.sslee.structure.routingslip

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

class SlipRouter(endActorRef: ActorRef) extends Actor with ActorLogging with RouteSlip {
  
  val paintBlack = context.actorOf(Props(new PaintCar("black")),"paintBlack")
  val paintGray = context.actorOf(Props(new PaintCar("gray")),"paintGray")
  val addNavigation = context.actorOf(Props(new AddNavigation),"addNavigation")
  val addParkingSensors = context.actorOf(Props(new AddParkingSensors),"addParkingSensors")
  
  private def createRouteSlip(options: Seq[CarOption.Value]) : Seq[ActorRef] = {
    log.debug(s"#####$self options is $options")
    
    if(!options.contains(CarOption.CAR_COLOR_GRAY))
      Seq(paintBlack, endActorRef)
    else {
      log.debug(s"#####$self choose options")
      options.foldLeft[List[ActorRef]](List.empty)((b,a) => a match {
        case CarOption.CAR_COLOR_GRAY => paintGray :: b
        case CarOption.NAVIGATION => addNavigation :: b
        case CarOption.PARKING_SENSORS => addParkingSensors :: b
      }) ++ Seq(endActorRef)
    }
  }
  
  def receive = {
    case Order(options: Seq[CarOption.Value]) =>
      val routeSlip = createRouteSlip(options)
      sendMessageToNextTask(routeSlip,new Car)
  }
}