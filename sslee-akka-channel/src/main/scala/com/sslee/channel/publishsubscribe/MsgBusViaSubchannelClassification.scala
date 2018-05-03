package com.sslee.channel.publishsubscribe

import akka.event.SubchannelClassification
import akka.event.EventBus
import com.sslee.channel.messages._
import akka.actor.ActorRef
import akka.util.Subclassification

class MsgBusViaSubchannelClassification extends EventBus with SubchannelClassification {
  
  type Event = News
  type Classifier = String
  type Subscriber = ActorRef
  
  //SubchannelClassification 의 abstract method
  //SubchannelClassification의 subscribe method 시 SubclassifiedIndex에서 사용 하는 것으로  
  //하위 class 여부인지, 같은 class인지 판별 여기서는 News 의 topic 의 String 의 startsWith로 구별 함.
  override protected val subclassification: Subclassification[Classifier] = new Subclassification[String] {
    def isEqual(x: String, y: String): Boolean = x == y
    def isSubclass(x: String, y: String): Boolean = x startsWith y
  }
  
  protected def classify(event: Event): Classifier = event.topic
  
  //이벤트의 해당 classifier에  등록된 모든 가입자에 대해 각 이벤트를 발행
  override protected def publish(event: Event, subscriber: Subscriber): Unit = 
    subscriber ! event.description
  
}