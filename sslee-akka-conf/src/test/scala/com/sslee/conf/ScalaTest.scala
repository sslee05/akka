package com.sslee.conf

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

class ScalaTest extends WordSpecLike with MustMatchers {
  
  "Configuration " must {
    "has configuration " in {
      val mySystem = ActorSystem("myTest")
      val config = mySystem.settings.config
      
      config getInt "myTest.initParam"  must be(20)
      config getString "myTest.applicationDesc"  must be("My Config Test")
      config getString "myTest.database.connect" must be("jdbc:mysql://localhost/mydata")
    }
    
    "has defaults" in {
      val mySystem = ActorSystem("myTestDefault")
      val config = mySystem.settings.config
      
      config getString "myTestDefault.applicationDesc" must be("My Current Test")
    }
    
    "included configuration " in {
      val mySystem = ActorSystem("myTestIncluded")
      val config = mySystem.settings.config
      
      config getString "myTestIncluded.applicationDesc" must be("My Current Test")
      config getInt "myTestIncluded.initParam" must be(20)
    }
    
    "Lift configuration " in {
      val configuration = ConfigFactory.load("lift")
      
      val system = ActorSystem("ConfSystem",configuration.getConfig("myTestLift").withFallback(configuration))
      val config = system.settings.config
      
      config getInt "myTest.initParam" must be(20)
      config getString "myTest.applicationDesc" must be("My Lift Test")
      config getString "myTestLift.rootParam" must be("root")
    }
    
    "default auto load Configuration" in {
      val system = ActorSystem("defaultAutoLoad")
      val config = system.settings.config
      
      config getString "jsonvalue.value01" must be("Override Jsonvalue")
      config getString "property.value01" must be("A")
    }
  }
  
}