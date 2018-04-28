package com.sslee.remote

import akka.remote.testkit.MultiNodeConfig


object ClientServerConfig extends MultiNodeConfig {
  
  val frontend2 = role("frontend") //frontend 역할 
  val backend2 = role("backend")   //backend 역할 
  
}