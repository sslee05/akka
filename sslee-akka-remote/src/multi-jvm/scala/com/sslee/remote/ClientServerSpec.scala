package com.sslee.remote

import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.Identify
import akka.actor.ActorIdentity

//frontend 에서 실행되는 명세서 
//주의할점 이름이 반드시 {TestName}MultiJvm{NodeName} 형식을 따라야 한다.
//아래는 ClientServer + MultiJvm + Frontend
class ClientServerSpecMultiJvmFrontend2 extends ClientServerSpec
//backend 에서 실행되는 명세서 
//주의할점 이름이 반드시 {TestName}MultiJvm{NodeName} 형식을 따라야 한다.
//아래는 ClientServer + MultiJvm + Backend
class ClientServerSpecMultiJvmBackend2 extends ClientServerSpec

//두 node가 공통으로 해야 하는 일을 기술하는 명세서 
class ClientServerSpec extends MultiNodeSpec(ClientServerConfig) 
  with MultiJvmBeforeAndAfterAll with ImplicitSender {
  
	import ClientServerConfig._
  
  //node(role) method는 test 도중 backend 역할을 하는 
  //node의 path를 반환 한다. 이 식은 ActorPath를 만든다.
  //주의할 점은 반드시 node method를 main test thread에서 호출 해야 함.
  //따라서 이 곳에서 선언.
  val backendNode = node(backend2)
  println(s"####################### $backendNode")
  
  //test에 참여하는 node 개수 
  def initialParticipants = roles.size
  
  "A Client Server configured app" must {
	  
	  "wait for all nodes to enter a barrier" in {
	    enterBarrier("startup") // 모든 node를 시작 한다.
	  }
	  
	  //backend 와 frontend node 를 test하기 위한 시나리오
	  "be able to create an event and sell a ticket" in {
	    runOn(backend2) { // <- 이 블럭이 있는 code를 backend JVM에서 실행 하게 한다.
	      //backend node 에서 BoxOffice를 만들어 frontend node의 
	      //RemoteLookupProxy가 boxOffice라는 이름으로 찾을 수 있게 한다.
	      val tt = system.actorOf(BoxOffice.props(Timeout(1 seconds)), "boxOffice")
	      println(s"############## ${tt.path}")
	      //backend node가 배포되었음을 알린다.
	      enterBarrier("deployed")
	    }
	    
	    runOn(frontend2) { // <- 이 블럭이 있는 code를 frontend JVM에서 실행 하게 한다.
	      
	      //backend node가 배포되기를 기다린다.
	      enterBarrier("deployed")
	      
	      val path = node(backend2) / "user" / "boxOffice"
	      val actorSelection = system.actorSelection(path)
	      
	      //actorSelection를 통해 boxOffice에게 Identify를 보내 생성 확인 하며 
	      //송신자를 testActor로 한다.
	      actorSelection tell (Identify(path), testActor)
	      
	      //expectMsgPF()를 통해 testActor의 receive를 확인 한다.
	      //여기서 원격의 boxOffice 에서 생성되었음을 알리는 응답 메시지를 기다린다.
	      val boxOfficeActorRef = expectMsgPF() {
	        case ActorIdentity(`path`, Some(boxOfficeRef)) => boxOfficeRef
	      }
	      
	      import BoxOffice._
	      import TicketSeller._
	      //원격에 있는 boxOffice에게 message를 보내며 test를 한다.
	      boxOfficeActorRef ! CreateEvent("RHCP", 2)
	      expectMsg(EventCreated(Event("RHCP", 2)))
	      
	      boxOfficeActorRef ! GetTickets("RHCP", 1)
	      expectMsg(Tickets("RHCP",Vector(Ticket(1))))
	    }
	    
	    //test 가 끝났음을 알리고 모든 node가 종료될 때까지 기다린다.
	    enterBarrier("finished")
	  }
	}
  
}