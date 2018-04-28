package com.sslee.remote

import com.typesafe.config.Config
import akka.util.Timeout

//설정 file에따라 Timeout 정책을 가져가기 위한 trait 
trait RequestTimeout {
  import scala.concurrent.duration._
  
  def configuredRequestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length,d.unit)
  }
}