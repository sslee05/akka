package com.sslee.consistence

package object routemessages {
  
  trait MyMessage[+A] {
    def id: A
    def values: Seq[A]
  }
  
  case class MyMessageImpl[String](id: String, values: Seq[String]) extends MyMessage[String]
  
  case class TimeoutMessage(id: String)
  case class MyRelayMessage(id: String, message: String)
}