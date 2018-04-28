package com.sslee.faulttolerance.case02

class MockDbCon(databaseUrl: String) {
  
  import Messages._
  val datas = (1L to 10L).map { i => 
    if(i != 8) (i -> DataValue("data_"+ i))
    //else (i -> new DbNodeDownException("db connection broken!!"))
    else (i -> new DbBrokenConnectionException("db connection broken!!"))
  }.toVector
  
  def write(key: Long): Unit = {
    println(s"##### MockDbCon=>${key}")
    val result = datas.filter(data => data._1 == key).foldLeft[Option[DbData]](None)((b,a) => Some(a._2))
    println(s"##### MockDbCon result =>${result}")
    result match {
      case data @ Some(DataValue(value)) => println(s"insert into mock_table values (${key},${value})")
      case _ => throw new DbNodeDownException("db connection broken!!")
    }
  }
  
  def close() = {
    println(s"database ${databaseUrl} is release connnection resource")
  }
}