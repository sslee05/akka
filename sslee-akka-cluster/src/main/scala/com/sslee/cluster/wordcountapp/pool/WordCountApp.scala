package com.sslee.cluster.wordcountapp.pool

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.actor.Props
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern._
import com.sslee.cluster.wordcountapp.messages._
import scala.util.Success
import scala.util.Failure


object WordCountApp {
  
  def main(args: Array[String]): Unit = {
    
    assert(!args.isEmpty && args.size >= 2)
    val configName = args(0)
    val port = args(1)
    
    val config = ConfigFactory.parseString(s"""
      akka.remote.netty.tcp.port=$port
      """).withFallback(ConfigFactory.load(configName))
      
    implicit val system = ActorSystem("words",config)
    implicit val ec = system.dispatcher
    implicit val timeout = Timeout(5 seconds)
    
    val roles = system.settings.config.getStringList("akka.cluster.roles")
    
    
    if(roles.contains("master")) {
      Cluster(system).registerOnMemberUp {
        val reducer = system.actorOf(Props(classOf[WordsReducer],3,system,timeout),"reducer")
        import akka.pattern._
        
        var nrRetries = 0
        
        val result = reducer ? StartCount("/Users/sslee/temp","test.txt")
        result onComplete {
          case Success(data) => 
            println(s"work count data: $data")
          case Failure(e) =>
            println(s"failed: ${e.getMessage}")
        }
      }
    }
  }
  
}