package com.sslee.faulttolerance.case02

import java.io.File

object Messages {
  
  trait DbData
  
  case class Start(file: FileData)
  case class DataValue(data: String) extends DbData
  case class FileLine(row: (String,String))
  case class FileData(lines: Vector[FileLine])
  
  @SerialVersionUID(1L)
  class DiskError(msg: String) extends Error(msg) with Serializable
  
  @SerialVersionUID(1L)
  class CorruptedFileException(msg: String, val file: FileData) extends Exception(msg) with Serializable
  
  @SerialVersionUID(1L)
  class DbNodeDownException(msg: String) extends Exception(msg) with DbData with Serializable
  
  @SerialVersionUID(1L)
  class DbBrokenConnectionException(msg: String) extends Exception(msg) with DbData with Serializable
  
}