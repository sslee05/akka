package com.sslee.cluster

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object WordsClusterSpecConfig extends MultiNodeConfig {
  
  val seed = role("seed")
  val master = role("master")
  val worker1 = role("worker-1")
  val worker2 = role("worker-2")
  
  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.cluster.metrics.collector-class = akka.cluster.JmxMetricsCollector  
  """))
}