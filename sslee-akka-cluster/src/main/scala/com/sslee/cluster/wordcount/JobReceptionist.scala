package com.sslee.cluster.wordcount

import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.Actor
import com.sslee.cluster.messages._
import java.net.URLEncoder
import akka.actor.ActorContext
import akka.actor.Terminated

class JobReceptionist(nrMaxRetires:Int) extends Actor with ActorLogging with MasterCreator {
  
  import context._
  
  var jobs = Set[Job]()
  var retires = Map.empty[String,Int]
  val encoding = "UTF-8"
  //var id = 0
  
  def receive = {
    case JobRequest(name, text) =>
      log.debug(s"##### reveive Reception $name $text")
      val masterName = s"master-${URLEncoder.encode(name,encoding)}"
      //id = id + 1
      val jobMaster = createMaster(masterName)
      
      jobs = jobs + Job(name, text, sender, jobMaster)
      
      jobMaster ! StartJob(name, text)
      watch(jobMaster)
      
    case WordCount(name,wordNrResult) =>
      jobs.find(job => job.name == name ).foreach{job =>
        job.responseTo ! JobSucess(name, wordNrResult)
        stop(job.master)
        jobs = jobs - job
      }
      
    case Terminated(jobMaster) => 
      jobs.find(job => job.master == jobMaster).foreach{ job =>
        val nrRetire = retires.getOrElse(job.name, 0)
        if(nrRetire <= nrMaxRetires) {
          
          //최대 retire까지 진행하도록 고의로 진행
          val modifiedText = job.text.filter(s => !s.contains("FAIL") || (nrRetire < nrMaxRetires) )
          
          jobs = jobs - job
          self.tell(JobRequest(job.name, modifiedText),job.responseTo)
          retires = retires + retires.get(job.name).map(n => job.name -> (n + 1)).getOrElse(job.name -> 1)
        }
        else {
          job.responseTo ! JobFailure(job.name)
        }
      }
  }
}

trait MasterCreator {
  def context: ActorContext
  def createMaster(name: String) = context.actorOf(Props(new JobMaster),name)
}