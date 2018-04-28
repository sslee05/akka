package com.sslee.future

object FutureTest {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  
  import scala.util._
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  
  val futureFail = Future { throw new OutOfMemoryError("error!") }
                                                  //> java.lang.OutOfMemoryError: error!
                                                  //| 	at com.sslee.future.FutureTest$.$anonfun$main$2(com.sslee.future.FutureT
                                                  //| est.scala:10)
                                                  //| 	at scala.concurrent.Future$.$anonfun$apply$1(Future.scala:653)
                                                  //| 	at scala.util.Success.$anonfun$map$1(Try.scala:251)
                                                  //| 	at scala.util.Success.map(Try.scala:209)
                                                  //| 	at scala.concurrent.Future.$anonfun$map$1(Future.scala:287)
                                                  //| 	at scala.concurrent.impl.Promise.liftedTree1$1(Promise.scala:29)
                                                  //| 	at scala.concurrent.impl.Promise.$anonfun$transform$1(Promise.scala:29)
                                                  //| 	at scala.concurrent.impl.CallbackRunnable.run(Promise.scala:60)
                                                  //| 	at scala.concurrent.impl.ExecutionContextImpl$AdaptedForkJoinTask.exec(E
                                                  //| xecutionContextImpl.scala:140)
                                                  //| 	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
                                                  //| 	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java
                                                  //| :1056)
                                                  //| 	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
                                                  //| 	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinW
                                                  //| Output exceeds cutoff limit.
  futureFail.map(value => println(value))         //> res0: scala.concurrent.Future[Unit] = Future(<not completed>)
  futureFail.onComplete {
    case Success(value) => println(value)
    case Failure(e) => println(e)
  }
  
  
}