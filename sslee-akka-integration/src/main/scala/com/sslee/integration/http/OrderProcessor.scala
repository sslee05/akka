package com.sslee.integration.http

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.integration.messages._

class OrderProcessor extends Actor with ActorLogging {
  
  val orders = new scala.collection.mutable.HashMap[Long, TrackingOrder]
  var trackingNum = 0L
  
  
  def receive = {
    case order: Order => 
     log.debug(s"##### OrderProcessor receive message order $order")
     trackingNum += 1
     
     val newTracking = TrackingOrder(trackingNum, "created", order)
     orders += trackingNum -> newTracking
     
     sender() ! newTracking
     
    case TrackingId(id) => 
      log.debug(s"##### OrderProcessor receive message TrackingId($id)) orders is $orders")
      orders.get(id) match {
        case Some(trackingOrder) =>
          log.debug(s"##### OrderProcessor receive message trackingOrder is ${trackingOrder.copy(status = "processing")}")
          sender() ! trackingOrder.copy(status = "processing")
        case _ => sender() ! NoSuchOrder(id)
      }
  }
  
  
}