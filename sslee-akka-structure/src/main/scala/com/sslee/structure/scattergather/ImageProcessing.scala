package com.sslee.structure.scattergather


import java.text.SimpleDateFormat
import java.util.Date

case class TimeoutMessage(timeoutMessage: String)

case class PhotoMessage(id: String, 
                        photo: String, 
                        createTime: Option[Date] = None, 
                        speed: Option[Int] = None)
                        
object ImageProcessing {
  
  val dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS")
  
  def getSpeed(image: String): Option[Int] = {
    val attribute = image split '|'
    if(attribute.size == 3)
      Some(attribute(1).toInt)
    else None
  }
  
  def getTime(image: String): Option[Date] = {
    val attribute = image split '|'
    if(attribute.size == 3)
      Some(dateFormat.parse(attribute(0)))
    else None
  }
  
  def getLicense(image: String): Option[String] = {
    val attribute = image split '|'
    if(attribute.size == 3)
      Some(attribute(2))
    else None
  }
  
  def createPhotoString(date: Date, speed:Int): String = 
    createPhotoString(date, speed, " ")
  
  def createPhotoString(date: Date, speed: Int, license: String): String = {
    val dateString = dateFormat.format(date)
    println(s"######dataFormatString:$dateString")
    "%s|%s|%s".format(dateString,speed,license)
  }
  
}