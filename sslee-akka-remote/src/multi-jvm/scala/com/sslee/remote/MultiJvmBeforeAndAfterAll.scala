package com.sslee.remote

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.{BeforeAndAfterAll,WordSpecLike}

trait MultiJvmBeforeAndAfterAll extends MultiNodeSpecCallbacks 
  with WordSpecLike with MustMatchers with BeforeAndAfterAll {
  
  //multiNodeSpec 의 before로 위임 시킨다.
  override def beforeAll() = multiNodeSpecBeforeAll()
  
  //multiNodeSpec 의 after로 위임 시킨다. 
  override def afterAll() = multiNodeSpecAfterAll()
  
}