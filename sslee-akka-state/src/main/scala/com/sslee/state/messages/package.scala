package com.sslee.state

package object messages {
  import akka.actor.ActorRef
  
  //event
  case class BookRequest(context: AnyRef, target: ActorRef)
  case class BookSupply(nrBook: Int)
  case object BookSupplySoldOut
  case object Done
  case object PendingRequests
  
  //responses
  case object PublisherRequest
  case class BookReply(context: AnyRef, reserveId: Either[String,Int])
  
  //states
  sealed trait State
  case object WaitForRequests extends State
  case object ProcessRequest extends State
  case object WaitForPublisher extends State
  case object SoldOut extends State
  case object ProcessSoldOut extends State
  
  //state Data
  case class StateData(nrBookInStore: Int, penddingRequests: Seq[BookRequest])
}