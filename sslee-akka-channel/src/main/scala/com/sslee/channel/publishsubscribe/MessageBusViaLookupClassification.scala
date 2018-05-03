package com.sslee.channel.publishsubscribe

import akka.event.LookupClassification
import akka.event.EventBus
import akka.event.ActorEventBus
import com.sslee.channel.messages._
import akka.event.SubchannelClassification
import akka.actor.ActorRef

class MessageBusViaLookupClassification extends EventBus 
  with LookupClassification with ActorEventBus {
  
  type Event = News
  type Classifier = String
  //이는 ActorEventBus를 mix-in 하면 구현 ActorRef로 구현 되어 있다.
  //따라서 여기처럼 같은 type이면 구현 할 필요가 없다.
  type Subscribe = ActorRef
  
  //LookupClassification 를 mix-in 했다면 반드시 구현 해야 한다.
  //인덱스 데이터 구조의 초기 크기를 결정
  def mapSize = 128
  
  //LookupClassification의 Index 집합에 구독자를 분류해서 넣을때 호출된다.
  //`java.lang.Comparable.compare`
  //LookupClassification에는 abstract로 되어있지만 ActorEventBus에 이미 아래와 같이 구현 되어있다.
  //따라서 여기처럼 같은 로직이면 구현 할 필요가 없다.
  override protected def compareSubscribers(a: Subscriber, b: Subscriber): Int  =
    a compareTo b

  //event 시 구독자를 선별하기 위해 publish에서 사용된다.
  protected def classify(event: Event) = 
    event.topic
  
  protected def publish(event: Event, subscriber: Subscriber): Unit = 
         subscriber ! event.description
}