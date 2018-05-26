package com.sslee.stream.marshaller

import com.sslee.stream.EventMarshalling
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.marshalling.{Marshaller,ToEntityMarshaller}
import akka.stream.scaladsl._
import akka.http.scaladsl.model.HttpEntity
import akka.util.ByteString
import spray.json._
import com.sslee.stream.resources._
import com.sslee.stream.helper.LogParseHelper._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.http.scaladsl.model.ContentTypeRange

object LogEntityMarshaller extends EventMarshalling {
  
  val jsContent = ContentTypes.`application/json`
  val txtContent = ContentTypes.`text/plain(UTF-8)`
    
  val supported = Set[ContentTypeRange](jsContent,txtContent)
  
  def createEventMarshaller(maxJsonObject: Int): ToEntityMarshaller[Source[ByteString,_]] = {
    
    val jsMarshaller: Marshaller[Source[ByteString,_], HttpEntity.Chunked] = 
      Marshaller.withFixedContentType(jsContent) {
        source: Source[ByteString, _] => HttpEntity(jsContent, source)
      }
    
    val txtMarshaller: Marshaller[Source[ByteString,_], HttpEntity.Chunked] = 
      Marshaller.withFixedContentType(txtContent) {
        source: Source[ByteString, _] => HttpEntity(txtContent, source)      
      }
    
    //2개의 marshaller를 이용하여 super marshaller를 만듬.
    Marshaller.oneOf(jsMarshaller, txtMarshaller)
  }
  
  def toText(src: Source[ByteString,_], maxJsonObject: Int): Source[ByteString,_] = { 
    val inFlow = JsonFraming.objectScanner(maxJsonObject)
      .map(b => b.decodeString("UTF-8"))
      .map(s => s.parseJson.convertTo[Event])
      
    val outFlow = Flow[Event].map(e => e.toJson.compactPrint).map(s => ByteString(s))
    
    val bidiFlow = BidiFlow.fromFlows(inFlow, outFlow)
    val flow = bidiFlow.join(Flow[Event])
    src.via(flow)
  }
  
  def createEventUnMarshaller(maxLine: Int, maxJsonObject: Int)  = {
    new Unmarshaller[HttpEntity, Source[Event, _]]  {
      def apply(entity: HttpEntity)(implicit es: ExecutionContext, mat: Materializer): Future[Source[Event, _]] = {
        val future = entity.contentType match {
          case ContentTypes.`application/json` =>
            Future.successful{
                JsonFraming.objectScanner(maxJsonObject)
                  .map(b => b.decodeString("UTF-8"))
                  .map(s => s.parseJson.convertTo[Event])
            }
            
          case ContentTypes.`text/plain(UTF-8)` =>
            Future.successful {
              Framing.delimiter(ByteString("\n"), maxLine)
                .map(b => b.decodeString("UTF-8"))
                .map(s => parseLogLine(s)).collect{case Some(e) => e}
            }
            
          case other =>
            println(s"###########=>$other")
            Future.successful {
              Framing.delimiter(ByteString("\n"), maxLine)
                .map(b => b.decodeString("UTF-8"))
                .map(s => parseLogLine(s)).collect{case Some(e) => e}
            }
            //Future.failed(new UnsupportedContentTypeException(supported)) 
        }
        
        future.map(flow => entity.dataBytes.via(flow))(es)
      }
    }.forContentTypes(supported.toList: _ *)
  }
  
}