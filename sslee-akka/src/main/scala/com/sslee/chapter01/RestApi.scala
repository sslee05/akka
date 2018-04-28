package com.sslee.chapter01

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.actor.ActorRef
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import BoxOffice._
import spray.json._


class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  
  implicit val requestTimeout = timeout
  implicit def executeContext = system.dispatcher
  
  def createBoxOffice() = system.actorOf(BoxOffice.props, BoxOffice.name)
}

trait RestRoutes extends BoxOfficeApi with EventMarshalling {
  
  def routes: Route = eventsRoute ~ eventRoute ~ ticketsRoute
  
  def eventsRoute = pathPrefix("events") {
    pathEndOrSingleSlash {
      get {
        //GET /events
        onSuccess(getEvents()) { events =>
          complete(events)
        }
      }
    }
  }
  
  def eventRoute = pathPrefix("events" / Segment) { event =>
    pathEndOrSingleSlash {
      post {
        //POST /events/:event
        //http Post localhost:5000/events/RCHP tickets:=10
        entity(as[EventDescription]) { eventDesc =>
          onSuccess(createEvent(event, eventDesc.tickets)) {
            case BoxOffice.EventCreated(event) => complete(event)
            case BoxOffice.EventExists => complete(Error(s"${event} event exists already.")) 
          }
        }
      } ~
      get {
        //GET /events/:event
        onSuccess(getEvent(event)) {
          _.fold(complete(NotFound))(e => complete(e))
        }
      } ~ 
      delete {
        //DELETE /events/:event
        onSuccess(cancelEvent(event)) {
          _.fold(complete(NotFound))(e => complete(e))
        }
      }
    }
  }
  
  def ticketsRoute = pathPrefix("events" / Segment / "tickets") { event =>
    post {
      //POST /events/:event/tickets
      pathEndOrSingleSlash {
        entity(as[EventDescription]) { eventDesc =>
          onSuccess(requestTickets(event,eventDesc.tickets)) { tickets =>
            if(tickets.entries.isEmpty) complete(NotFound)
            else complete(tickets)
          }
        }
      }
    }
  }
  
}


trait BoxOfficeApi {
  
  def createBoxOffice(): ActorRef
  
  implicit def executeContext: ExecutionContext
  implicit def requestTimeout: Timeout
  
  lazy val boxOffice = createBoxOffice()
  
  def createEvent(event: String, nrOfTicket: Int): Future[EventResponse] = 
    boxOffice.ask(CreateEvent(event,nrOfTicket)).mapTo[EventResponse]
  
  def getEvents(): Future[Events] = 
    boxOffice.ask(GetEvents).mapTo[Events]
  
  def getEvent(event: String): Future[Option[Event]] = 
    boxOffice.ask(GetEvent(event)).mapTo[Option[Event]]
  
  def cancelEvent(event: String): Future[Option[Event]] = 
    boxOffice.ask(CancelEvent(event)).mapTo[Option[Event]]
  
  def requestTickets(event: String, tickets: Int): Future[TicketSeller.Tickets] = 
    boxOffice.ask(GetTickets(event,tickets)).mapTo[TicketSeller.Tickets]
}