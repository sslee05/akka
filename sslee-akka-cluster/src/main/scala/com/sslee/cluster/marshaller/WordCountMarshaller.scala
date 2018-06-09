package com.sslee.cluster.marshaller

import spray.json._
import com.sslee.cluster.messages._

trait WordCountMarshaller extends DefaultJsonProtocol {
  
  implicit val wordCountFormat = jsonFormat2(WordCount)
  
}

object TestMashall extends App with WordCountMarshaller {
  
  val wordCount = WordCount("test", Map("a" -> 1,"b" -> 2))
  val result = wordCount.toJson
  println(result)
}