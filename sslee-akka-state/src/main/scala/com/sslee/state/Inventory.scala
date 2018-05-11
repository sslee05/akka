package com.sslee.state

import scala.collection.Seq

import com.sslee.state.messages._

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.FSM
import scala.concurrent.duration._

class Inventory(publisher: ActorRef) extends Actor with FSM[State, StateData] {
  
  var reserveId = 0
  
  startWith(WaitForRequests, new StateData(0, Seq()))
  
  when(WaitForRequests) {
    case Event(request: BookRequest, data: StateData) => 
      log.debug(s"##### when WaitForRequests to Event(request: BookRequest, data: StateData)")
      val newStateData = data.copy(penddingRequests = data.penddingRequests :+ request)
      Thread.sleep(1000L)
      if(newStateData.nrBookInStore > 0)
        goto(ProcessRequest) using newStateData
      else
        goto(WaitForPublisher) using newStateData
        
    case Event(PendingRequests, data: StateData) => 
      log.debug(s"##### when WaitForRequests to Event(PendingRequests, data: StateData)")
      if(data.penddingRequests.isEmpty) 
        stay
      else if(data.nrBookInStore > 0)
        goto(ProcessRequest)
      else
        goto(WaitForPublisher)
  }
  
  when(WaitForPublisher,stateTimeout = 4 seconds) {
    case Event(supply: BookSupply, data: StateData) => 
      log.debug(s"##### when WaitForPublisher to Event(supply: $supply, data: $data)")
      goto(ProcessRequest) using data.copy(nrBookInStore = supply.nrBook)
    case Event(BookSupplySoldOut, data: StateData) => 
      log.debug(s"##### when WaitForPublisher to Event(BookSupplySoldOut, data: $data)")
      goto(ProcessSoldOut)
    case Event(StateTimeout, _) => 
      log.debug(s"##### when WaitForPublisher timout to Event(StateTimout, _)")
      goto(WaitForRequests)
      
  }
  
  when(ProcessRequest) {
    case Event(Done, data: StateData) => 
      log.debug(s"##### when ProcessRequest to Event(Done, data: StateData)")
      goto(WaitForRequests) using data.copy(nrBookInStore = data.nrBookInStore -1 , 
            penddingRequests = data.penddingRequests.tail)
  }
  
  when(SoldOut) {
    case Event(request: BookRequest, data: StateData) =>
      log.debug(s"##### when SoldOut to Event(request: BookRequest, data: StateData)")
      goto(ProcessSoldOut) using new StateData(0, Seq(request))
  }
  
  when(ProcessSoldOut) {
    case Event(Done, data: StateData) =>
      log.debug(s"##### when ProcessSoldOut to Event(Done, data: StateData)")
      goto(SoldOut) using new StateData(0,Seq())
  }
  
  whenUnhandled {
    //위의 예제에서는 ProcessRequest, ProcessSoldOut, WaitForPublisher 일때
    case Event(request: BookRequest, data: StateData) =>
      log.debug(s"##### whenUnhandled request:$request data:$data")
      stay using data.copy(penddingRequests = data.penddingRequests :+ request)
    
    case Event(e , s) =>
      log.warning(s"##### whenUnhandled pass event event:$e data:$s")
      stay 
  }
  
  //val tt = WaitForRequests -> ProcessRequest
  
  onTransition {
    case _ -> WaitForRequests => 
      log.debug(s"##### onTransition to WaitForRequests")
      if(!nextStateData.penddingRequests.isEmpty)
        self ! PendingRequests
    
    case _ -> WaitForPublisher =>
      log.debug(s"##### onTransition to WaitForPublisher")
      publisher ! PublisherRequest
    
    case _ -> ProcessRequest =>
      log.debug(s"##### onTransition to ProcessRequest")
      val request = nextStateData.penddingRequests.head
      reserveId += 1
      request.target ! new BookReply(request.context, Right(reserveId))
      self ! Done
      
    case _ -> ProcessSoldOut => 
      log.debug(s"##### onTransition to ProcessSoldOut")
      nextStateData.penddingRequests.foreach{request => 
        request.target ! new BookReply(request.context, Left("SoldOut"))  
      }
      self ! Done
      
    case _ -> SoldOut =>
      log.debug(s"##### onTransition to SoldOut")
  }
  
  initialize
  
  onTermination {
    case StopEvent(FSM.Normal, state, data) =>
      log.debug(s"#####stopEvent Normal state:$state data:$data")
    case StopEvent(FSM.Shutdown, state, data) =>
      log.debug(s"#####stopEvent Shutdown state:$state data:$data")
    case StopEvent(FSM.Failure(cause), state, data) =>
      log.debug(s"#####stopEvent Failure $cause state:$state data:$data")
  }
  
}