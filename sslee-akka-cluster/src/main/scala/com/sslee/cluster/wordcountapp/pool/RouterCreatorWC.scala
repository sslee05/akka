package com.sslee.cluster.wordcountapp.pool

import akka.actor.Actor
import akka.cluster.routing.ClusterRouterPool
import akka.routing.BalancingPool
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.actor.Props
import akka.routing.RoundRobinPool

trait RouterCreatorWC { this: Actor =>
  import context._
  
  def routerWordCount = 
    context.actorOf(ClusterRouterPool(
      RoundRobinPool(5),//init routee count in pool
      ClusterRouterPoolSettings(
        totalInstances = 1000, //모든 node의 routee갯수의 총합의 쵀대 갯 수 
        maxInstancesPerNode = 20, // node당 routee의 최대  갯 수 
        allowLocalRoutees = false, // 해당 node에 routee 생성 할 수 있게 허용할지 여부
        useRole = Some("worker") // routee가 배포될 node의 role 
      )
    ).props(Props[WordCounter]),"wordCountRouter")
}