package com.example.akkahttptemplate

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.LoggerFactory

object Main 
  extends App
  with StrictLogging 
{

  // print logback's internal status
  val lc = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
  StatusPrinter.print(lc)

  logger.info("Starting server...")
  Server.run(Config.app.thisServer.host, Config.app.thisServer.port)
}
