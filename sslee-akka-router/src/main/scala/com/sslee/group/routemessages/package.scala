package com.sslee.group

package object routemessages {
  case class MyMessage(msg: String)
  case class MyReplyMessage(msg: String)
  
  case class CreateRoutee(test:String)
  
  case class Resizing(size: Int)
  case object ResetRoutee
}