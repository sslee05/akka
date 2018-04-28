package com.sslee.remote

import akka.actor.Actor
import akka.actor._
import akka.actor.Props
import akka.actor.ActorLogging
import akka.event.Logging

class TicketSeller(event: String) extends Actor {
  
  import TicketSeller._
  import context._
  
  val logger = Logging(system.eventStream,"TicketSeller")
  var tickets: Vector[Ticket] = Vector.empty
  
  
  def receive = {
    case Add(newTickets) => 
      logger.debug(s"##### TicketSelller actor receive message Add(${tickets})")
      tickets = tickets ++ newTickets
      
    case Buy(ticketCount) => 
      logger.debug(s"##### TicketSelller actor receive message Buy(${tickets})")
      val soldTicket = tickets.take(ticketCount)
      if(soldTicket.length >= ticketCount) {
        sender() ! Tickets(event, soldTicket)
        tickets = tickets.drop(ticketCount)
      }
      else sender() ! Tickets(event)
      
    case GetEvent =>
      sender() ! Some(BoxOffice.Event(event, tickets.size))
      
    case Cancel =>
      sender() ! Some(BoxOffice.Event(event, tickets.size))
  }
}

object TicketSeller {
  
  def props(event: String) = Props(new TicketSeller(event))
  
  case class Ticket(id: Int)
  case class Add(tickets: Vector[Ticket])
  case class Buy(tickets: Int)
  case class Tickets(event: String, entries: Vector[Ticket] = Vector.empty)
  case object GetEvent
  case object Cancel
}

