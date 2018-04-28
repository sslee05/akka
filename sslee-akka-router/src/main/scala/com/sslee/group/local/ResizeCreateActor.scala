package com.sslee.group.local

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.ActorSelectionRoutee
import akka.routing.AddRoutee
import com.sslee.group.routemessages._
import akka.routing.RemoveRoutee
import akka.routing.GetRoutees
import akka.routing.Routees
import akka.routing.ActorRefRoutee
import akka.actor.PoisonPill
import akka.actor.Terminated

class ResizeCreateActor(nrActors: Int,props: Props, router: ActorRef) extends Actor with ActorLogging  {
  
  //routee 갯수 
  var nrRoutees = nrActors
  var nrChildInstance = 0
  
  
  override def preStart() {
    log.debug("############ start resizeCreateActor preStart")
    super.preStart()
    (0 until nrRoutees).map(nr => createRoutee())
  }
  
  //routee를 생성 한다. 
  def createRoutee() {
    
    nrChildInstance += 1
    val child = context.actorOf(props, s"myRoutee-$nrChildInstance")
    
    /*
     routee 생성 
		 router 에게 알리고 router에 생성된 routee를 추가 하라고 이야기 한다.
		 이때 알일 수 있는 방법은 router 에게 AddRoutee(routee: Routee)를 송신  
		 Routee 유형은 3가지가 있으며 
		 1.	ActorRefRoutee(ref: ActorRef) => routee 종료시 Terminated message가 router에 가므로 creator가 알지 못 한다.
		 2. ActorSelectionRoutee(selection: ActorSelection) => 이것을 이용 
		 3. ServeralRoutees(routees: immutable.IndexedSeq[Routee])
     */
    val selection = context.actorSelection(child.path)
    log.debug(s"############ child.path ${child.path} selection $selection router $router")
    router ! AddRoutee(ActorSelectionRoutee(selection))
    context.watch(child)
  }
  
  def receive = {
    
    //resize 요구가 왔을 경우 
    case Resizing(size) => {
      //줄이라는 요구시 
      if(size < nrRoutees) {
        
        context.children.take(nrRoutees - size).foreach {routee => 
          val selection = context.actorSelection(routee.path)
          //router에게 삭제된 routee를 알린다.
          router ! RemoveRoutee(ActorSelectionRoutee(selection))
        }
          
        //router 에게 현재 routee가 가지고 있는 routee목록을 요청하여 
        //creator 가 가지고 있는 routee을 Terminated 시킨다. 
        //이는  GetRoutess의 응답으로 Routees를 받아 처리 한다.
        router ! GetRoutees
      }
      //else if(size == nrRoutees) log.debug("request resize size equals now state size")
      else {
        (nrRoutees until size).map(n => createRoutee())
      }
      
      nrRoutees = size
    }
    
    //router에게 GetRoutees를 요청하면 응답 받는 경우 이다. 
    case routees: Routees => {
      import collection.JavaConverters._
      //응답유형은 Routees 의 하위 구현체 인데, 위에서 ActorSelectionRoutee를 사용하기 로 했으므로 
      //ActorSelectionRoutee가 case가 실행 될 것 이다.
      var activeRoutees = routees.getRoutees.asScala.map {
        case actor: ActorRefRoutee => actor.ref.path.toString
        case actor: ActorSelectionRoutee => actor.selection.pathString
      }
      
      /*
       아래는 다음 2가지 일을 한다.
       1.resize를 통한 router에서 제거된 routee를 creator에서도 제거
       2.routee가 어떠한 이유로 stop된경우 router가 고의로 중지 한 것이 아니면 
         다시 생성 하고 router에 생성을 알린다. 
       */
      for(child <- context.children) {
        val idx = activeRoutees.indexOf(child.path.toStringWithoutAddress)
        if(idx >= 0)
          activeRoutees.remove(idx) //creator 에서 새로 생성할 routee 목록에서 제거 
        else 
          child ! PoisonPill // router에 없는 routee는 creator에서 제거
      }
      activeRoutees.foreach { terminated => 
        val name  = terminated.substring(terminated.lastIndexOf("/")+1)
        val newChild = context.actorOf(props,name)
        context.watch(newChild)
      }
    }
    
    //creator의 child(routee)가 중지 신호가 왔을 경우 router가 고의로 중지한 것인지 알아본다. 
    case Terminated(child) =>
      router ! GetRoutees
      
  }
  
}