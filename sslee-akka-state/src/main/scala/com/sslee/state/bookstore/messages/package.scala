package com.sslee.state.bookstore

package object messages {

  import akka.actor.ActorRef
  
  //Event
  case class RequestBook(nrBook: Int, target: ActorRef)
  case class BookSupply(nrBook: Int)
  case object BookSoldOut
  case object Done
  case object PenddingRequests
  
  //State
  sealed trait State
  case object WaitRequest extends State
  case object ProcessRequest extends State
  case object WaitPublisher extends State
  case object SoldOut extends State
  
  //
  case object RequestPublisher
  case class BookReply(nrBook: Int, reservedId: Either[String, Int])
  
  //StateData
  case class StateData(nrBookInStore: Int, penddingRequest: Seq[RequestBook])
}