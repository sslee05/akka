package com.sslee.integration

package object messages {
  case class Order(customerId: String, productId: String, number: Int)
  case class TrackingOrder(orderId: Long, status: String, order: Order)
  case class TrackingId(id: Long)
  case class NoSuchOrder(id: Long)
}