package com.sslee.cluster.wordcountapp.pool

import akka.actor.Actor
import akka.routing.BalancingPool
import akka.actor.Props

trait LocalRouterCreatorWC { this: Actor => 
  
  import context._
  
  def routerWordCount = 
    system.actorOf(BalancingPool(5).props(Props[WordCounter]),"wordCountRouter")
  
}