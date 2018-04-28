package com.sslee.chapter01

import akka.actor.Actor
import akka.actor._
import akka.actor.Props
import akka.actor.PoisonPill
import akka.event.Logging

class TicketSeller(event: String) extends Actor {
  import TicketSeller._
  import context._
  
  val logger = Logging(system.eventStream,"TicketSellerActor")
  
  var tickets = Vector.empty[Ticket]
  
  def receive = {
    case Add(newTicket) => {
      logger.debug(s"###### TicketSellerActor Add event etc sender(${sender().path})")
      tickets = tickets ++ newTicket
    }
    case Buy(nrOfTickets) =>
      logger.debug(s"###### TicketSellerActor Buy event etc sender(${sender().path})")
      val entries = tickets.take(nrOfTickets)
      if(entries.size >= nrOfTickets) {
        logger.debug(s"###### TicketSellerActor call Sender(${sender().path}) Tickets and Drop Tickets")
        sender() ! Tickets(event,entries)
        tickets = tickets.drop(nrOfTickets)
      }
      else sender() ! Tickets(event)
      
    case GetEvent => {
      logger.debug(s"###### TicketSellerActor GetEvent event and call Sender Event Tickets etc sender(${sender().path})")
      sender() ! Some(BoxOffice.Event(event,tickets.size))
    }
    case Cancel => 
      logger.debug(s"###### TicketSellerActor Cancel event and call Sender Event Tickets and terminate etc sender(${sender().path})")
      sender() ! Some(BoxOffice.Event(event,tickets.size))
      self ! PoisonPill
      
  }
}

object TicketSeller {
  
  def prop(event: String) = Props(new TicketSeller(event))
  
  case class Ticket(id: Int)
  case class Add(tickets: Vector[Ticket])
  case class Buy(tickets: Int)
  case class Tickets(event: String, entries: Vector[Ticket] = Vector.empty[Ticket])
  case object GetEvent
  case object Cancel
}