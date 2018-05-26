package com.sslee.stream.chapter.quickstart

import akka.stream._
import akka.stream.scaladsl._

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.{Paths,Path}
import scala.util.Failure
import scala.util.Success


object ExplorationSource extends App {
  
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  
  //(1 to 100) 까지의 Int를 열린출력으로 내보냄을 나타내는 설계도 
  //NotUsed는 실체화 값을 가지지 않는다. 
  //이는 설계도 일뿐 실행시 실제로 처리가 진행 
  val source: Source[Int, NotUsed] = Source(1 to 100)
  
  //처리가 진행 
  //처리 결과의 실체화는 Future[Done] 
  val result: Future[Done] = source.runForeach(i => println(i))(materializer)
  
  result.onComplete{case Success(a) => println(a) case Failure(e) => println(e)}(system.dispatcher) 
  
  //Source는 Graph이며 stream처리 설계도 이고 열린 output 출력을 가진다. 
  val factorial:Source[BigInt, NotUsed] = source.scan(BigInt(1))((ac, i) => ac * i)
  
  //materializer를 가지고 실행시 return type는 처리한 byte수의 Future[IOResult] 이다.
  val fileResult: Future[IOResult] = factorial.map(num => ByteString(s"$num\n"))
    .runWith(FileIO.toPath(Paths.get("/Users/sslee/temp/factorial.txt")))
  
  //종료후 actorSystem은 내려가지 않으므로 완료시 actorSystem를 종료
  fileResult.onComplete{ iors => 
    println("@@@@"+iors)
    system.terminate()
  }(system.dispatcher)
   
}