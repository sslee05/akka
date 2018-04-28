package com.sslee.remote.pool

import akka.remote.testkit.MultiNodeConfig

object PoolMultiNodeConfig extends MultiNodeConfig {
  
  val frontend = role("frontend")
  val backend = role("backend")
}