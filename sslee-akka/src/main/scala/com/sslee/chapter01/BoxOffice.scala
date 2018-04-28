package com.sslee.chapter01

import akka.actor.Props
import akka.util.Timeout
import akka.actor._
import akka.event.Logging
import scala.concurrent.Future


object BoxOffice {
  
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"
  
  case class CreateEvent(name: String, tickets: Int)// ticket 생성 
  case class GetEvent(name: String)
  case object GetEvents
  case class GetTickets(event: String, tickets: Int)// ticket 구매 
  case class CancelEvent(name: String)
  
  case class Event(name: String, tickets: Int)
  case class Events(events: Vector[Event])
  
  sealed trait EventResponse
  case class EventCreated(event: Event) extends EventResponse
  case object EventExists extends EventResponse
}

class BoxOffice(implicit timeout: Timeout) extends Actor {
  import BoxOffice._
  import context._
  
  val logger = Logging(system.eventStream,"BoxOfficeLogger")
  
  def createTicketSeller(name: String) = 
    context.actorOf(TicketSeller.prop(name),name)
    
  def receive = {
    // ticket 생성 
    case CreateEvent(name, tickets) =>
      logger.debug(s"###### boxOfficeActor CreateEvent event ${name} etc sender(${sender().path}) ")
      def create() = {
        val ticketActor = createTicketSeller(name)
        val newTickets = (1 to tickets).map { id => TicketSeller.Ticket(id) }.toVector
        
        logger.debug(s"###### boxOfficeActor call TicketSeller Add etc sender(${sender().path})")
        ticketActor ! TicketSeller.Add(newTickets)
        
        logger.debug(s"###### boxOfficeActor call Sender eventCreated etc sender(${sender().path})")
        sender() ! EventCreated(Event(name,tickets))
      }
      
      context.child(name).fold(create())(_ => sender() ! EventExists)
    
    // ticket 구매 
    case GetTickets(event,tickets) => 
      logger.debug(s"###### boxOfficeActor GetTickets event ${event} etc sender(${sender().path})")
      def notFound() = {
        logger.debug(s"###### boxOfficeActor call Sender Empty Ticket etc sender(${sender().path})")
        sender() ! TicketSeller.Tickets(event)
      }
      def buy(child: ActorRef) = {
        logger.debug(s"###### boxOfficeActor forward TicketSeller Buy etc sender(${sender()})")
        child.forward(TicketSeller.Buy(tickets))
      }
        
      context.child(event).fold(notFound())(buy)
    
    //ticket 조회 
    case GetEvent(event) => 
      logger.debug(s"###### boxOfficeActor GetEvent event etc sender(${sender().path})")
      def notFound() = {
        logger.debug(s"###### boxOfficeActor call Sender None etc sender(${sender().path})")
        sender() ! None
      }
      def getEvent(child: ActorRef) = {
        logger.debug(s"###### boxOfficeActor forward TicketSeller GetEvent etc sender(${sender().path})")
        child forward TicketSeller.GetEvent
      }
      
      context.child(event).fold(notFound())(getEvent)
      
    //ticket 목록 조회 
    case GetEvents =>
      logger.debug(s"###### boxOfficeActor GetEvents event etc sender(${sender().path})")
      import akka.pattern.ask
      import akka.pattern.pipe
      
      def getEvents:Iterable[Future[Option[Event]]] = {
        logger.debug(s"###### boxOfficeActor call self call GetEvent child etc sender(${sender().path})")
        context.children.map(child => self.ask(GetEvent(child.path.name)).mapTo[Option[Event]])
      }
      
      def convertToEvents(future: Future[Iterable[Option[Event]]]) =
        future.map(iter => iter.flatten).map(iter => Events(iter.toVector))
      
      pipe(convertToEvents(Future.sequence(getEvents))) to sender()
  }
}
