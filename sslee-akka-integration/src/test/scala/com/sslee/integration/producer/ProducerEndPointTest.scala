package com.sslee.integration.producer

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import com.sslee.integration.StopSystemAfterAll
import org.scalatest.WordSpecLike
import scala.concurrent.duration._
import akka.util.Timeout
import akka.testkit.TestProbe
import com.sslee.integration.messages._
import com.sslee.integration.consumer._
import com.sslee.integration.producer._
import akka.actor.Props
import akka.camel.CamelExtension
import scala.concurrent.Future
import akka.actor.ActorRef
import scala.concurrent.Await

class ProducerEndPointTest extends TestKit(ActorSystem("ProducerSyste")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  //activationFuturFor를 위한 implicit 선언 
  implicit val ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 10 seconds
  
  "Producer EndPoint" must {
    "using TCP request and response callback" in {
      //결과 검증 probe
      val probe = TestProbe()
      //중앙 공통 interface 
      val camelUri = "mina2:tcp://localhost:8080?textline=true"
      
      //요청을 받아 서비스를 처리하는 system의 consumerEndPoint
      val consumer = system.actorOf(Props(new ResponseConsumerEndPoint(camelUri,probe.ref)))
      //서비스 요청을 할 system의 producer EndPoint
      val producer = system.actorOf(Props(new ProducerEndPoint(camelUri)))
      
      //camel component를 확장한다.
      val activatedSystem = CamelExtension(system)
      //consumer camel을 확장한 ActorRef
      val activatedCons: Future[ActorRef] = activatedSystem.activationFutureFor(consumer)
      //producer camel을 확장한 ACtorRef
      val activatedProd: Future[ActorRef] = activatedSystem.activationFutureFor(producer)
      
      val camels: Future[List[ActorRef]] = Future.sequence(List(activatedCons, activatedProd))
      Await.result(camels,3 seconds)
      
      val requester = TestProbe()
      val msg = new Order("sslee", "Akka in Action", 10)
      requester.send(producer, msg)
      
      probe expectMsg msg
      requester expectMsg "OK"
      
      system.stop(consumer)
      system.stop(producer)
    }
  }
  
}