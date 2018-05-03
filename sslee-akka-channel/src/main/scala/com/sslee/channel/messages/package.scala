package com.sslee.channel

package object messages {
  
  case class Order(number: Int)
  case class News(topic: String, description: String)
  
}