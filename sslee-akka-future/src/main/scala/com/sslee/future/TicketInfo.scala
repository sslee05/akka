package com.sslee.future

import org.joda.time.{Duration, DateTime}

case class TicketInfo(ticketNr: String,
                      userLocation: Location,
                      event: Option[Event]=None,
                      travelAdvice: Option[TravelAdvice]=None,
                      weather: Option[Weather]=None,
                      suggestions: Seq[Event]=Seq()) 
                      
case class Location(lat: Double, lot: Double)

case class Event(name: String, location: Location, time: DateTime)

case class Artist(name: String, calendarUri: String)

case class RouteByCar(rout: String,
                     timeToLeave: DateTime,
                     origin: Location,
                     destination: Location,
                     estimatedDuration: Duration,
                     trafficJamTime: Duration )

case class PublicTransportAdvice(advice: String, timeToLeave: DateTime, origin: Location, destination: Location,
                                 estimatedDuration: Duration)

case class TravelAdvice(routeByCar: Option[RouteByCar]=None,
                        publicTransportAdvice:  Option[PublicTransportAdvice]=None)

case class Weather(temperature: Int, precipitation: Boolean)
