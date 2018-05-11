package com.sslee.state.bookstore

import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.state.bookstore.messages._
import math.min

class Publisher(nrTotalBook: Int, nrRequestBook: Int) extends Actor with ActorLogging {
  
  var nrLeftBook = nrTotalBook
  
  def receive = {
    case RequestPublisher => 
      if(nrLeftBook <= 0)
        sender() ! BookSoldOut
      else {
        val nrBook = min(nrRequestBook, nrLeftBook)
        nrLeftBook -= nrBook
        
        sender() ! BookSupply(nrBook)
      }
  }
  
}