package com.example.akkahttptemplate

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import java.util.concurrent.CountDownLatch
import scala.concurrent.Future

object Server extends ThingRoutes {

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = ActorSystem("akkahttptemplate-actorsystem")
//  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  // Create the thing service.
  // We insert a test Thing right at the beginning for ease of demonstration: 
  val thingService: ThingService = new ThingServiceImpl(Thing("hello", "world!"))

  // Runs the server and safely shuts down.
  // Helps when running in sbt.
  val latch: CountDownLatch = new CountDownLatch(1)
  private def runServerAndShutdown(httpBinding: Future[Http.ServerBinding]) = {
  
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
  
      logger.info("Terminating actor system and http routes...")
      httpBinding
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
  
      logger.info("Done terminating server.")
      latch.countDown()
  
      logger.info("Done")
    }))
  
    try {
      latch.await()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(1)
    }
    System.exit(0)
  }

  /** Run the server.
    *
    */
  def run(host: String, port: Int) = {

    val routes = thingRoutes

    val httpBinding: Future[Http.ServerBinding] =
      Http().bindAndHandle(routes, host, port)

    logger.info(s"Server online at http://$host:$port/")

    runServerAndShutdown(httpBinding)
  }
}


