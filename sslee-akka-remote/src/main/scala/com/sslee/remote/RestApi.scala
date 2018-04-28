package com.sslee.remote

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.http.scaladsl.server._
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern.ask
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import BoxOffice._
import akka.event.Logging

/*
class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends RestRoutes  {
  implicit val requestTimeout = timeout
  implicit def executeContext = actorSystem.dispatcher
  
  def createBoxOffice() = actorSystem.actorOf(BoxOffice.props, BoxOffice.name)
}
*/

trait RestApi extends BoxOfficeApi with EventMarshalling {
  
  
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
  
  //abstract method로 두어 boot에서 이를 상속하고 원격, local 에 따른 다른 구현을 가져가게 한다.
  def createBoxOffice(): ActorRef
  
  //astract method boot 에서 boot에서 이를 상속하고 원격, local 에 따른 다른 구현을 가져가게 한다.
  implicit def executeContext: ExecutionContext
  //asbtract method boot 에서 boot에서 이를 상속하고 원격, local 에 따른 다른 구현을 가져가게 한다.
  implicit def requestTimeout: Timeout
  
  lazy val boxOffice = createBoxOffice()
  
  // 등록 
  def createEvent(event: String, ticketNr: Int): Future[EventResponse] = 
    (boxOffice ask CreateEvent(event,ticketNr)).mapTo[EventResponse]
  
  // 구매 
  def requestTickets(event: String, ticketNr: Int): Future[TicketSeller.Tickets] = 
    (boxOffice ask BuyTicket(event,ticketNr)).mapTo[TicketSeller.Tickets]
  
  // 조회 
  def getEvent(event: String) : Future[Option[Event]] = 
    (boxOffice ask GetEvent(event)).mapTo[Option[Event]]
  
  // event 목록 조회 
  def getEvents(): Future[Events]  = {
    println("########RestApi getEvents()")
    (boxOffice ask GetEvents).mapTo[Events]
  }
}