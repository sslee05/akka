package com.sslee.cluster.stream

import akka.stream.scaladsl._
import akka.stream.IOResult
import java.nio.file.Paths
import akka.util.ByteString
import scala.concurrent.Future

object WordCountStream {
  
  def fileSource(fileName: String): Source[String, Future[IOResult]] = {
    val path = Paths.get("/Users/sslee/temp/", fileName)
    FileIO.fromPath(Paths.get("/Users/sslee/temp/", fileName))
        .via(Framing.delimiter(ByteString("\n"),1024 * 1024)) map (b => b.decodeString("UTF-8"))
  }
  
}