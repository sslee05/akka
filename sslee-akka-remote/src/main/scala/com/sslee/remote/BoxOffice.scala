package com.sslee.remote

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.actor.Props
import akka.event.Logging
import akka.actor.ActorRef
import scala.concurrent.Future

class BoxOffice(implicit timeout: Timeout) extends Actor {

  import context._
  import BoxOffice._
  
  val logger = Logging(system.eventStream,"BoxOffice")
  
  def createTicketSeller(event: String) = 
    context.actorOf(TicketSeller.props(event),event)
    
  def receive = {
    //등록 
    case CreateEvent(name, tickets) => 
      logger.debug(s"############## GetEvents message CreateEvent(${name}, ${tickets})")
      def createAndSend = {
        val child = createTicketSeller(name)
        watch(child)
        
        val newTickets = (1 to tickets).map(TicketSeller.Ticket(_)).toVector
        child forward TicketSeller.Add(newTickets)
        sender() ! EventCreated(Event(name,newTickets.size))
      }
      
      context.child(name).fold(createAndSend)(child => sender() ! EventExists) 
    
    // 구매 sub TicketSeller.Tickets
    case GetTickets(name,tickets) => 
      logger.debug(s"############## GetEvents message GetTickets(${name},{tickets})")
      context.child(name).fold(sender() ! TicketSeller.Tickets(name))(child => child forward TicketSeller.Buy(tickets))
      
    // 구매 Future[TicketSeller.Tickets]
    case BuyTicket(name, tickets) => 
      logger.debug(s"############## GetEvents message BuyTicket(${name},${tickets})")
      def getTicket: Option[Future[TicketSeller.Tickets]] = 
        context.child(name).map(child => (self ask GetTickets(name,tickets)).mapTo[TicketSeller.Tickets])
      
      def sequence[T](op: Option[Future[T]]) : Future[Option[T]] = 
        op.foldLeft[Future[Option[T]]](Future(None))((b,a) => a.map(a1 => Some(a1)))
      
      pipe(sequence(getTicket).map(op => op getOrElse TicketSeller.Tickets(name))) to sender()
     
    // 조회 sub Option[Event]
    case GetEvent(name) => 
      logger.debug(s"############## GetEvents message GetEvent(${name})")
      def getEvent(child: ActorRef) = {
        logger.debug(s"############## GetEvents message GetEvent(${name}) getEventMethod ${child}")
        child forward TicketSeller.GetEvent
      }
      def notFound = sender() ! None
      
      context.child(name).fold(notFound){child => {
        logger.debug(s"############## GetEvents message GetEvent(${name}) in Option.fold in")
        getEvent(child)}
      }
    
    // 조회 Future[Events] 
    case GetEvents => 
      logger.debug("############## GetEvents message received")
      val rs: Iterable[Future[Option[Event]]] = 
        context.children.map(child => (self ask GetEvent(child.path.name)).mapTo[Option[Event]])
      
      pipe(Future.sequence(rs).map(rs => rs.flatten).map(rs => Events(rs.toVector))) to sender()
  }
  
}

object BoxOffice {
  
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"
  
  case class CreateEvent(name: String, tickets: Int)// ticket 생성 
  case class GetEvent(name: String)
  case object GetEvents
  case class GetTickets(event: String, tickets: Int)// ticket 구매
  case class BuyTicket(event: String, tickets: Int)// ticket 구매 
  case class CancelEvent(name: String)
  
  case class Event(name: String, tickets: Int)
  case class Events(events: Vector[Event])
  
  sealed trait EventResponse
  case class EventCreated(event: Event) extends EventResponse
  case object EventExists extends EventResponse
}
