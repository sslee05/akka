package com.sslee.cluster.router.pool

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.actor.Props
import com.sslee.cluster.router.messages._

object ExPoolApp  {
  
  def main(args: Array[String]) : Unit = {
    val conf = args.head
    val port = args.tail.head
    println(s"########## $conf $port")
    val config = ConfigFactory.parseString(s"""
          akka.remote.netty.tcp.port=$port
        """).withFallback(ConfigFactory.load(conf))
    
        println(s"##########config port ${config.getString("akka.remote.netty.tcp.port")}")    
    val system = ActorSystem("words", config)
    
    val roles = system.settings.config.getStringList("akka.cluster.roles")
    
    if(roles.contains("master")) {
      Cluster(system).registerOnMemberUp {
        val userActor = system.actorOf(Props[UserActor],"userActor")
        val deadLetterSub = system.actorOf(Props[StateDeadLetter],"deadLetterSub") 
        val stateService = system.actorOf(Props[StateService02],"stateService")
        println("########## Master ready and start job")
        stateService ! StateMessage("#####TICK#####",userActor)
        //stateService ! StatesJob("First we prepare a reusable Flow that will")
      }
    }
  }
}