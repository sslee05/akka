package aia.testdriven

import org.scalatest.{BeforeAndAfterAll,Suite}
import akka.testkit.TestKit

trait StopSystemAfterAll extends BeforeAndAfterAll { this: TestKit with Suite => 
  override protected def afterAll() {
    super.afterAll()
    system.terminate()
  }
}