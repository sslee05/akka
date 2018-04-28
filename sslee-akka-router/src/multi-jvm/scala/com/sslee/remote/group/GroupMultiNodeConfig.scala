package com.sslee.remote.group

import akka.remote.testkit.MultiNodeConfig

object GroupMultiNodeConfig extends MultiNodeConfig {
  
  val frontend = role("frontend")
  val backend = role("backend")
}