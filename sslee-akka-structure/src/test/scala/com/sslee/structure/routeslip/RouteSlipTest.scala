package com.sslee.structure.routeslip

import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike
import akka.actor._

import com.sslee.structure.StopSystemAfterAll

import akka.testkit.TestKit
import akka.testkit.TestProbe
import com.sslee.structure.routingslip.SlipRouter
import com.sslee.structure.routingslip.Order
import com.sslee.structure.routingslip.Car
import com.sslee.structure.routingslip.CarOption

class RouteSlipTest extends TestKit(ActorSystem("RouteSlipTest")) 
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "RouteSlip struct pattern " must {
    "RouteSlip basic pattern" in {
      
      val probe = TestProbe()
      val router = system.actorOf(Props(new SlipRouter(probe.ref)),"slipBasicRouter")
      
      val basicOrder = new Order(Seq())
      val defaultCar = Car( 
          color = "black",
          hasNavigation = false,
          hasParkingSensors = false
      )
      
      router ! basicOrder
      
      probe expectMsg defaultCar
    }
    
    "RouteSlip option pattern " in {
      
      val probe = TestProbe()
      val router = system.actorOf(Props(new SlipRouter(probe.ref)),"fulloptionSlipRouter")
      
      val fullOption = new Order(Seq(
          CarOption.CAR_COLOR_GRAY,
          CarOption.NAVIGATION, 
          CarOption.PARKING_SENSORS))
      
      val fullOptionCar = Car (
          color = "gray",
          hasNavigation = true,
          hasParkingSensors = true
      )
      
      router ! fullOption
      
      probe expectMsg fullOptionCar
    }
  }
}