package com.sslee.state

import scala.math.min

import com.sslee.state.messages._

import akka.actor.Actor
import akka.actor.ActorLogging

class Publisher(totalNrBooks: Int, nrBooksPerRequest: Int) extends Actor with ActorLogging {
  
 var nrLeft = totalNrBooks
 
 def receive = {
   case PublisherRequest =>
     log.debug(s"####### publisher published $nrLeft")
     //Thread.sleep(5000L)
     if(nrLeft == 0 )
       sender() ! BookSupplySoldOut
     else {
       val supply = min(nrBooksPerRequest, nrLeft)
       nrLeft -= supply
       sender() ! BookSupply(supply)
     }
 }
  
}