package com.sslee.state

package object routemessages {
  case object RouteStateRight
  case object RouteStateLeft
  
  case class MyMessage(msg: String)
}