package com.sslee.cluster.wordcountapp.pool

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.pattern._
import com.sslee.cluster.wordcountapp.messages._
import akka.actor.ReceiveTimeout
import akka.util.Timeout
import akka.stream.scaladsl._
import scala.concurrent.Future
import akka.stream.IOResult
import java.nio.file.Paths
import akka.util.ByteString
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import scala.util.Success
import scala.util.Failure
import akka.actor.ActorRef
import scala.util.Try
import akka.routing.Broadcast

class WordsReducer(nrRetires: Int)(implicit system: ActorSystem,timeout: Timeout) extends Actor with ActorLogging with RouterCreatorWC {

  import context._
  
  val wordCounter = routerWordCount
  var mergeWC = Map.empty[String,Int]
  implicit val mat = ActorMaterializer()
  
  def receive = {
    case StartCount(path, fileName) => 
      log.info(s"#####WordReducer-startCount:StartCount($path,$fileName)  $sender")
      pipe(count(path,fileName)) to sender()
      mergeWC = Map.empty[String,Int]
  }
  
  def count(path: String, fileName: String): Future[Map[String,Int]] = 
    source(path,fileName)
        .mapAsync(parallelism = 5)(line => (wordCounter ? TaskData(line)).mapTo[WordCount])
        .runWith(Sink.foldAsync[Map[String,Int],WordCount](Map.empty){ (ma, wc) =>
          Future(wc.data.keys.foldLeft(ma)((b,key) => b.updated(key, b.getOrElse(key, 0) + wc.data.getOrElse(key, 0))))
        })
    
  def source(path: String, fileName: String): Source[String, Future[IOResult]] = {
    log.info(s"#####WordReducer-source called path:$path fileName:$fileName")
    FileIO.fromPath(Paths.get(path,fileName))
      .via(Framing.delimiter(ByteString("\n"), 1024 * 1024))
      .map(b => b.decodeString("UTF-8"))
  }
}