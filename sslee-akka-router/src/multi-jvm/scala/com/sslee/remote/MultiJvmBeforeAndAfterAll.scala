package com.sslee.remote

import org.scalatest.BeforeAndAfter
import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll

trait MultiJvmBeforeAndAfterAll extends MultiNodeSpecCallbacks 
  with WordSpecLike with MustMatchers with BeforeAndAfterAll {
  
  //before hock을 multiNodeSpec에게 위임 
  override def beforeAll() = multiNodeSpecBeforeAll()
  
  //after hock을 multiNodeSpec에게 위임 
  override def afterAll() = multiNodeSpecAfterAll()
  
}