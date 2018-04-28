package com.sslee.conf

import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._

object BootHellow extends App {
  
  val system = ActorSystem("hellokernel")
  val actor = system.actorOf(Props[HellowWorld])
  val config = system.settings.config
  val timer = config.getInt("hellowWorld.timer")
  system.actorOf(Props(new HellowWorldCaller(timer millis, actor)))
  
}