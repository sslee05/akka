package com.sslee.integration.consumer

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerRegistry
import org.apache.activemq.camel.component.ActiveMQComponent
import org.apache.commons.io.FileUtils
import org.scalatest.MustMatchers
import org.scalatest.WordSpecLike

import com.sslee.integration.StopSystemAfterAll
import com.sslee.integration.messages.Order

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.camel.CamelExtension
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.util.Timeout
import javax.jms.Connection
import javax.jms.DeliveryMode
import javax.jms.Session

class ConsumerEndpointTest extends TestKit(ActorSystem("camelSystem"))
  with WordSpecLike with MustMatchers with StopSystemAfterAll {
  
  "ConsumerEndpoint" must {
    "file consumer test" in {
      
      //camelExtension.activationFutureFor에서 필요한 항목들
      implicit val executionContext = system.dispatcher
      implicit val timeout:Timeout = 10 seconds
      
      val probe = TestProbe()
      val camelUri = "file:/Users/sslee/temp/"
      val consumer = system.actorOf(Props(new ConsumerEndPoint(camelUri, probe.ref)))
      
      val camelExtension = CamelExtension(system)
      val activated: Future[ActorRef] = camelExtension.activationFutureFor(consumer)
        
     
      val msg = Order("sslee", "Akka in Action", 10)
      val xml = 
        <order>
					<customerId>{msg.customerId}</customerId>
          <productId>{msg.productId}</productId>
          <number>{msg.number}</number>
				</order>
          
      
      val msgFile = new File("/Users/sslee/temp/","order-20180512.xml")
      FileUtils.write(msgFile, xml.toString.replace("\n",""))
      
      Await.ready(activated, 5 seconds)
      
      probe.expectMsg(msg)
    }
    
    "TCP protocol change" in {
      
      implicit val executionContext = system.dispatcher
      implicit val timeout:Timeout = 10 seconds
      
      val probe = TestProbe()
      val camelUri = "mina2:tcp://localhost:8080?textline=true&sync=false"
      val consumer = system.actorOf(Props(new ConsumerEndPoint(camelUri, probe.ref)))
      
      val activated = CamelExtension(system).activationFutureFor(consumer)
      Await.ready(activated, 5 seconds)
      
      val msg = Order("sslee", "Akka in Action", 10)
      val xml = 
        <order>
					<customerId>{msg.customerId}</customerId>
          <productId>{msg.productId}</productId>
          <number>{msg.number}</number>
				</order>
      
      val xmlStr = xml.toString.replace("\n","")
      val socket = new Socket("localhost",8080)
      val outputWriter = new PrintWriter(socket.getOutputStream,true)
      outputWriter.println(xmlStr)
      outputWriter.flush()
      
      probe.expectMsg(msg)
      
      outputWriter.close()
      //system.stop(consumer)
    }
    
    "TCP Reponse change" in {
      
      implicit val executionContext = system.dispatcher
      implicit val timeout: Timeout = 10 seconds
      
      val probe = TestProbe()
      val camelUri = "mina2:tcp://localhost:8081?textline=true&sync=true"
      val consumer = system.actorOf(Props(new ResponseConsumerEndPoint(camelUri, probe.ref)))
      
      val activated = CamelExtension(system).activationFutureFor(consumer)
      Await.ready(activated, 5 seconds)
      
      val msg = Order("sslee","Akka in Action",10)
      val xml = 
        <order>
					<customerId>{msg.customerId}</customerId>
          <productId>{msg.productId}</productId>
          <number>{msg.number}</number>
				</order>
          
      val xmlStr = xml.toString().replace("\n","")
      val socket = new Socket("localhost",8081)
      val outputWriter = new PrintWriter(socket.getOutputStream,true)
      outputWriter.println(xmlStr)
      outputWriter.flush()
      
      val responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val response = responseReader.readLine
      response must be ("<confirm>OK</confirm>")
      
      responseReader.close
      outputWriter.close
      
    }
    
    "use activeMQ" in {
      
      implicit val executionContext = system.dispatcher
      implicit val timeout: Timeout = 10 seconds
      
      //camelContext를 얻어 mq broker component를 등록한다. 
      val camelExtension = CamelExtension(system) 
      val camelContext = camelExtension.context
      val producerTemplate = camelExtension.template
      
      camelContext.addComponent("activemq", 
           ActiveMQComponent.activeMQComponent("vm://localhost:8082?broker.persistent=false"))
      
      val probe = TestProbe()
      val camelUri = "activemq:queue:akkacamelDST"
      val consumer = system.actorOf(Props(new ConsumerEndPoint(camelUri, probe.ref)))
      
      val activated = camelExtension.activationFutureFor(consumer)
      Await.ready(activated, 5 seconds)
      
      val msg = Order("sslee", "Akka in Action", 10)
      val xml = 
        <order>
					<customerId>{msg.customerId}</customerId>
          <productId>{msg.productId}</productId>
          <number>{msg.number}</number>
				</order>
      
      val xmlStr = xml.toString.replace("\n","")
      producerTemplate.sendBody(xmlStr)
      //sendMQMessage(xmlStr)
      probe.expectMsg(msg)
      
      val brokers = BrokerRegistry.getInstance().getBrokers
      brokers.forEach((name,broker) => broker.stop())
      
    }
  }
  
  def sendMQMessage(msg: String): Unit = {
    // Create a ConnectionFactory
    val connectionFactory =
      new ActiveMQConnectionFactory("tcp://localhost:8082");

    // Create a Connection
    val connection: Connection = connectionFactory.createConnection()
    connection.start()

    // Create a Session
    val session = connection.createSession(false,
      Session.AUTO_ACKNOWLEDGE)

    // Create the destination (Topic or Queue)
    val destination = session.createQueue("xmlTest");

    // Create a MessageProducer from the Session to the Topic or Queue
    val producer = session.createProducer(destination);
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

    // Create a messages
    val message = session.createTextMessage(msg);

    // Tell the producer to send the message
    producer.send(message);

    // Clean up
    session.close();
    connection.close();
  }

}