package com.sslee.channel.publishsubscribe


import akka.event.ScanningClassification
import akka.event.EventBus
import akka.actor.ActorRef
import com.sslee.channel.messages._

class MsgViaScanningClassification extends EventBus with ScanningClassification {
  
  type Event = Order
  type Classifier = Int
  type Subscriber = ActorRef
  
  //10권 이상 구입에 대한 Classifier는 2권 구입한 Classifier 분류에도 속해야 한다.
  //따라서 비교 결과가 크면 minus 값을 해야 한다. 
  protected def compareClassifiers(a: Classifier, b: Classifier): Int =
    if (a > b) -1 else if (a == b) 0 else 1
    
  protected def compareSubscribers(a: Subscriber, b: Subscriber): Int =
    a.compareTo(b) 
    
  protected def matches(classifier: Classifier, event: Event): Boolean =
    event.number >= classifier
    
  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }
  
}