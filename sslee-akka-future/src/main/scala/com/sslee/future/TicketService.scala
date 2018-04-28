package com.sslee.future

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime
import scala.util.control.NonFatal

trait TicketService extends WebService {
  
  type Recovery[T] = PartialFunction[Throwable,T]
  def withNone[T]: Recovery[Option[T]] = { case NonFatal(e) => None }
  def withEmptySeq[T]: Recovery[Seq[T]] = { case NonFatal(e) => Seq.empty[T] }
  def withPrevious[T](previous: T): Recovery[T] = { case NonFatal(e) => previous }
  
  def getTicketInfo(ticketNr: String, location: Location): Future[TicketInfo] = {
    
    val emptyTicketInfo = TicketInfo(ticketNr, location)
    val eventInfo: Future[TicketInfo] = getEvent(ticketNr, location).recover(withPrevious(emptyTicketInfo))
    
    //def getWeather(ticketInfo: TicketInfo): Future[TicketInfo]
    //def getTravelAdvice(ticketInfo: TicketInfo, event: Event): Future[TicketInfo]
    //def getSuggestions(event: Event): Future[Seq[Event]]
    
    eventInfo.flatMap { info =>
      
      val weatherFt: Future[TicketInfo] = getWeather(info)
      val adviceFt : Future[TicketInfo] = weatherFt.flatMap { ticket =>
          ticket.event.map(event => getTravelAdvice(ticket,event)).getOrElse(weatherFt)
      }
      
      adviceFt.flatMap{ ticket => 
        ticket.event.map(event => getSuggestions(event).map(rs => ticket.copy(suggestions = rs))).getOrElse(adviceFt)  
      }
    }
  }
  
  /**
   * 날씨 service 2곳을 호출하여 빨리 도착하는 것을 취한다.
   * TicketInfo.weather: Option[Weather]
   */
  def getWeather(ticketInfo: TicketInfo): Future[TicketInfo] = {
    
    val weatherX: Future[Option[Weather]] = callWeatherServiceX(ticketInfo).recover(withNone)
    val weatherY: Future[Option[Weather]] = callWeatherServiceY(ticketInfo).recover(withNone)
    
    //firstCompleted
    //성공적 결과나 실패나 상관없이 먼저 결과가 도착한 것을 처리 한다.
    val xs = List(weatherX, weatherY)
    //Future.firstCompletedOf(xs).map(weatherOp => ticketInfo.copy(weather = weatherOp))
    
    //find 결과 도출이 먼저된 것에 대하여 처리를 한다(단 실패가 먼저시 무시하고 성공적인 결과를 기다린다)
    Future.find(xs)(wOp => !wOp.isEmpty).map(oop => 
      oop.map(wOp1 => ticketInfo.copy(weather = wOp1)).getOrElse(ticketInfo))
  }
  
  /**
   * 교통상황과 대중교통의 정보를 제공한다.
   * TicketInfo.travelAdvice:Option(TravelAdvice[(Option[RouteByCar], Option[PublicTransportAdvice)]])
   */
  def getTravelAdvice(ticketInfo: TicketInfo, event: Event): Future[TicketInfo] = {
    
    val routeByCar: Future[Option[RouteByCar]] = 
      callTraficService(ticketInfo.userLocation, event.location, event.time).recover(withNone)
      
    val publicTstAdvice: Future[Option[PublicTransportAdvice]] = 
      callPublicTransportService(ticketInfo.userLocation, event.location, event.time).recover(withNone)
      
    routeByCar.zip(publicTstAdvice).map {
      case (rOp, pOp) => ticketInfo.copy(travelAdvice = Some(TravelAdvice(rOp,pOp)))
    }
    
  }
  
  def getSuggestions(event: Event): Future[Seq[Event]] = {
    val artists: Future[Seq[Artist]] = callSimilarArtistsService(event).recover(withEmptySeq[Artist])
    //def callCalendarService(artist: Artist, nearLocation: Location): Future[Event]
    
    artists.flatMap(rs => Future.traverse(rs){ artist => 
      callCalendarService(artist, event.location)  
    })
  }
  
  def getSuggestionsViaSequence(event: Event): Future[Seq[Event]] = {
    val artists: Future[Seq[Artist]] = callSimilarArtistsService(event)
    artists.flatMap(artistXs => Future.sequence(artistXs.map(a => callCalendarService(a,event.location))))
  }
}

trait WebService {
  
  def getEvent(ticketNr: String, location: Location): Future[TicketInfo]
  
  def callWeatherServiceX(ticketInfo: TicketInfo): Future[Option[Weather]]
  def callWeatherServiceY(ticketInfo: TicketInfo): Future[Option[Weather]]
  
  def callTraficService(originLocation: Location, destination: Location, time: DateTime): Future[Option[RouteByCar]]
  def callPublicTransportService(originLocation: Location, destination: Location, time: DateTime): Future[Option[PublicTransportAdvice]]
  
  def callSimilarArtistsService(event: Event): Future[Seq[Artist]]
  def callCalendarService(artist: Artist, nearLocation: Location): Future[Event]
}