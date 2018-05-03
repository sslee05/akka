package com.sslee.channel.publishsubscribe

import org.scalatest.MustMatchers
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import com.sslee.channel.StopSystemAfterAll
import akka.testkit.TestProbe
import com.sslee.channel.messages._
import akka.actor.ActorRef
import akka.testkit.TestActor
import akka.testkit.ImplicitSender
import scala.concurrent.duration._

class LookupClassificationTest extends TestKit(ActorSystem("ClassficationSystem")) 
  with WordSpecLike with MustMatchers with ImplicitSender with StopSystemAfterAll {
  
  "EventBus implements " must {
    "LookupClassfication " in {
      
      val musicSubscriber = TestProbe()
      val recipeSubscriber = TestProbe()
      
      val eventBus = new MessageBusViaLookupClassification()
      eventBus.subscribe(musicSubscriber.ref, "musicNews")
      eventBus.subscribe(musicSubscriber.ref, "recipe")
      eventBus.subscribe(recipeSubscriber.ref, "recipe")
      
      eventBus publish News("musicNews","Jeo Satriani music festival 2018.05.02 pm 07:00")
      
      musicSubscriber.expectMsg("Jeo Satriani music festival 2018.05.02 pm 07:00")
      recipeSubscriber.expectNoMessage(3 seconds)
      
      eventBus publish News("recipe","Kimchi helthy food and delicious")
      
      musicSubscriber.expectMsg("Kimchi helthy food and delicious")
      recipeSubscriber.expectMsg("Kimchi helthy food and delicious")
      
      
    }
  }
  
}