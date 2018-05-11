package com.sslee.state.bookstore

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.FSM
import com.sslee.state.bookstore.messages._
import scala.concurrent.duration._

class BookStore(publisher: ActorRef) extends Actor with FSM[State, StateData]{
  
  var reserveId = 0
  
  startWith(WaitRequest, new StateData(0, Seq()))
  
  when(WaitRequest) {
    case Event(event: RequestBook, stateData: StateData) =>
      log.debug(s"#####when WaitRequest event $event stateData $stateData")
      val newStateData = stateData.copy(penddingRequest = stateData.penddingRequest :+ event)
      if(newStateData.nrBookInStore > 0)
        goto(ProcessRequest) using newStateData
      else
        goto(WaitPublisher) using newStateData
        
    case Event(PenddingRequests, stateData: StateData) =>
      log.debug(s"#####when WaitRequest event PenddingRequests stateData $stateData")
      if(stateData.penddingRequest.isEmpty)
        stay
      else if(stateData.nrBookInStore > 0)
        goto(ProcessRequest)
      else 
        goto(WaitPublisher)
  }
  
  when(WaitPublisher,stateTimeout = 4 seconds) {
    case Event(event: BookSupply, stateData: StateData) =>
      log.debug(s"#####when WaitPublisher event $event stateData $stateData")
      val newStateData = stateData.copy(nrBookInStore = stateData.nrBookInStore + event.nrBook)
      goto(ProcessRequest) using newStateData
      
    case Event(BookSoldOut, stateData: StateData) =>
      log.debug(s"#####when WaitPublisher event BookSoldOut stateData $stateData")
      goto(SoldOut)
      
    case Event(StateTimeout, _) => 
      log.debug(s"##### when WaitForPublisher timout to Event(StateTimout, _)")
      goto(WaitRequest)
  }
  
  when(ProcessRequest) {
    case Event(Done, stateData: StateData) =>
      log.debug(s"#####when ProcessRequest event Done stateData $stateData")
      val newStateData = stateData.copy(nrBookInStore = stateData.nrBookInStore - 1, 
          penddingRequest = stateData.penddingRequest.tail)
          
      goto(WaitRequest) using newStateData
  }
  
  when(SoldOut) {
    case Event(Done, stateData: StateData) =>
      log.debug(s"#####when SoldOut event Done stateData $stateData")
      goto(WaitRequest) using StateData(0, Seq())
  }
  
  whenUnhandled {
    case Event(event: RequestBook, stateData: StateData) =>
      log.debug(s"whenUnhandled event $event stateData $stateData")
      stay using stateData.copy(penddingRequest = stateData.penddingRequest :+ event)
      
    case Event(e, s) =>
      log.debug(s" skip event event is $e stateData $s")
      stay
  }
  
  onTransition {
    case es -> WaitRequest =>
      log.debug(s"#####onTransition from $es to WaitRequest")
      if(!nextStateData.penddingRequest.isEmpty)
        self ! PenddingRequests
        
    case es -> WaitPublisher =>
      log.debug(s"#####onTransition from $es to WaitPublisher")
      publisher ! RequestPublisher
      
    case es -> ProcessRequest =>
      log.debug(s"#####onTransition from $es to ProcessRequest")
      val request = nextStateData.penddingRequest.head
      reserveId += 1
      request.target ! BookReply(request.nrBook, Right(reserveId))
      self ! Done
      
    case es -> SoldOut =>
      log.debug(s"#####onTransition from $es to SoldOut")
      nextStateData.penddingRequest.foreach(req => req.target ! BookReply(req.nrBook, Left("Sold Out")))
      self ! Done
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