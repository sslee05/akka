package com.sslee.cluster.http


import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import akka.util.Timeout
import com.sslee.cluster.messages._
import akka.pattern._
import akka.stream.scaladsl._
import akka.stream.IOResult
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sslee.cluster.marshaller.WordCountMarshaller
import com.sslee.cluster.stream.WordCountStream._
import spray.json._
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import scala.util.Success
import akka.http.scaladsl.model.StatusCodes
import scala.util.Failure


trait WordCountApi extends WordCountServiceApi {
  
  def routes = getCount
  
  def getCount = path("wordcount") {
    get {
      parameters('filename.as[String]) { fileName =>
        onComplete(processWordCount(fileName)){
          case Success(wc) => complete((StatusCodes.OK,wc.toJson.compactPrint))
          case Failure(e) =>
            println(s"##########e=> ${e.getMessage}")
            complete((StatusCodes.BadRequest,"error"))
        }
      }
    }
  }
  
}

trait WordCountServiceApi extends WordCountMarshaller {  
  
  def createReceptionist: ActorRef
  implicit def executionContext: ExecutionContext
  implicit def system: ActorSystem
  implicit def requestTimeout : Timeout
  implicit def mat = ActorMaterializer()
  
  lazy val receptionist = createReceptionist
  
  def processWordCount(pathName: String): Future[WordCount] = {
    println("###################")
    fileSource(pathName).mapAsync(parallelism = 5)(str => (receptionist ? JobRequest("startJob",List(str))).mapTo[WordCount])
      .toMat(Sink.reduce[WordCount]{(b,a) => 
        b.copy(wordNrResult = {
          (b.wordNrResult.keys ++ a.wordNrResult.keys).foldLeft[Map[String,Int]](Map.empty)((mb,key) => 
            mb.updated(key,b.wordNrResult.getOrElse(key, 0) + a.wordNrResult.getOrElse(key, 0)))
        })  
      })(Keep.right).run()
  }

}