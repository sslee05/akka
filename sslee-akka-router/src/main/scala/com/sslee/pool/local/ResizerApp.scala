package com.sslee.pool.local

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import akka.routing.FromConfig
import akka.routing.Broadcast
import akka.actor.PoisonPill

object ResizerApp extends App {
  
  import com.sslee.pool.routemessges._
  
  val system = ActorSystem("resizeActorSystem", ConfigFactory.load("local-pool-resizer"))
  
  val router = system.actorOf(FromConfig.props(Props[ResizeRouteeActor]),"localRouter")
  val senderActor = system.actorOf(Props(new SenderActor(router)))
  
  //모든 routeee mailbox 에 message가 들어 가도록 한다.
  senderActor ! MyMessage(s"message 0")
  senderActor ! MyMessage(s"message 1")
  senderActor ! MyMessage(s"message 2")
  
  //messages-per-resize = 1 로 설정 했으므로 
  // 이번 message들 때는 판단을 유보 한다. 
  // 이번 message들은 모두 2개의  routee mail box로 간다.,
  // 그 후 부터는 message들은 다시 resize 판단의 기준다.
  (0 to 8) foreach { i =>
    senderActor ! MyMessage(s"overload message $i")
  }
  
  //resize는 동기적으로 일어나지 않기 때문에  reoutee를 추가하기전에 routee가 먼저 일을 끝낼 수 있다.
  //따라서 시간을 두어 모든 message가 현재 2개의 routee mail box로 모두 들어가지 않게 한다.
  //설정에 router가 routee message를 주기 전에 
  //현재 모든 routee들의 mail box가 message1개 있을 경우에 size up이 발생한다. 
  Thread.sleep(1000L)
  (0 to 8) foreach { i =>
    senderActor ! MyMessage(s"overload message $i$i")
  }
  
  println("send end")
  
}