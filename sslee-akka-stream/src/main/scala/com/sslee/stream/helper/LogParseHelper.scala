package com.sslee.stream.helper

import com.sslee.stream.resources._
import java.time.ZonedDateTime

object LogParseHelper {
  
  def parseLogLine(line: String): Option[Event] = {
    if(line.isEmpty) None
    else {
      line.split("\\|") match {
        case Array(host,service, state, time, desc, tag, metric) => 
          val t = tag.trim
          val m = metric.trim
          
          Some(Event(
            host.trim,
            service.trim,
            state.trim match {
              case State(s) => s
              case _        => throw new Exception(s"Unexpected state: $line")
            },
            ZonedDateTime.parse(time.trim),
            desc.trim,
            if(t.nonEmpty) Some(t) else None,
            if(m.nonEmpty) Some(m.toDouble) else None
          ))
        case Array(host, service, state, time, desc) => 
          Some(Event(
            host.trim,
            service.trim,
            state.trim match {
              case State(s) => s
              case _        => throw new Exception(s"Unexpected state: $line")
            },
            ZonedDateTime.parse(time.trim),
            desc.trim
          ))
         case _ => 
            throw new LogParseException(s"Failed on line: $line")
      }
    }
  }
  
  def logLine(event: Event) = {
    s"""${event.host} | ${event.service} | ${State.norm(event.state)} | ${event.time.toString} | ${event.description} ${if(event.tag.nonEmpty) "|" + event.tag.get else "|" } ${if(event.metric.nonEmpty) "|" + event.metric.get else "|" }\n"""
  }
}