package com.sslee.consistence

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.MustMatchers
import com.sslee.StopSystemAfterAll
import org.scalatest.WordSpecLike
import akka.actor.Props
import akka.routing.ConsistentHashingPool
import scala.concurrent.duration._
import akka.testkit.ImplicitSender
import com.sslee.consistence.routemessages._
import com.sslee.consistence.routemessages.MyMessage
import com.sslee.consistence._
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope

class HashMappingTest extends TestKit(ActorSystem("HashMappingSystem")) 
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "consistent router" must {
    "use partial function ConsistentHashMapping" in {
      
      val router = system.actorOf(
        ConsistentHashingPool(5,
            virtualNodesFactor = 3,
            hashMapping = {case msg: MyMessage[String] => msg.id}).props(Props(new Aggregator(3 seconds))),
            "localRouter"
      )
      
      router ! MyMessageImpl("1",Seq("msg01"))
      router ! MyMessageImpl("1",Seq("msg02"))
      
      expectMsg(MyMessageImpl("1", Seq("msg01","msg02")))
      
    }
    
    "use ConsistentHashable " in {
      
      case class MyMessageHashingKey[String](id: String, values: Seq[String]) 
        extends MyMessage[String] with ConsistentHashable {
        override def consistentHashKey: Any = id
      }
      
      val router = system.actorOf(
        ConsistentHashingPool(5,virtualNodesFactor = 3).props(
          Props(new Aggregator(3 seconds))),"localRouter2")
        
      router ! MyMessageHashingKey[String]("1",Vector("msg01"))
      router ! MyMessageHashingKey[String]("1",Vector("msg02"))
      
      expectMsg(MyMessageImpl("1",Vector("msg01","msg02")))
    }
    
    "use ConsistentHashableEnvelope" in {
      
      val router = system.actorOf(
          ConsistentHashingPool(5,virtualNodesFactor = 3).props(
              Props(new Aggregator(3 seconds))),"localRouter3")
              
      router ! ConsistentHashableEnvelope( 
       message =  MyMessageImpl("01",Vector("msg01")), hashKey = "01")
        
      router ! ConsistentHashableEnvelope(
        message = MyMessageImpl("01", Vector("msg02")), hashKey = "01")
        
      expectMsg(MyMessageImpl("01", Vector("msg01", "msg02")))
          
      
    }
  }
  
}