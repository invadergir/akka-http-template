package com.example.akkahttptemplate

import pureconfig._
import pureconfig.configurable._

// app-wide settings / configuration object
object Config {

  implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  //implicit val myEnumExampleType = ConfigReader.fromString[MyEnumExample.Value](
  //  ConvertHelpers.catchReadError(s => MyEnumExample.withName(s)))
  //implicit val converterOffsetDateTime = offsetDateTimeConfigConvert(DateTimeFormatter.ISO_DATE_TIME)
  //implicit val converterlocalDateTime = localDateTimeConfigConvert(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  // global app configuration
  val app: AppConfig = pureconfig.loadConfigOrThrow[AppConfig]("akkahttptemplate")
  app.validate()
  println(s"App Configuration is $app")
  
  /** Helper method to check a boolean and throw if not true.
    * 
    */
  def check(boolean: Boolean, errorMessage: String = "Configuration Error!"): Unit = {
    if ( !boolean ) {
      throw new RuntimeException(errorMessage)
    }
  }
}

import Config.check

// top-level config object
case class AppConfig(thisServer: ServerConnection) {

  /** validate
    */
  def validate() = {
    thisServer.validate()
  }
}

case class ServerConnection(
  protocol: String = "http",
  host: String,
  port: Int,
) {

  /**
   * Simple check on the connection parameters. 
   */
  def validate(): Unit = { 
    check(host.nonEmpty && port > 0 && protocol.nonEmpty, 
      "ServerConnection instance is invalid: "+this.toString)
  }

  /** Get the host and port for use in connection strings.
    * 
    */
  val hostPort: String = {
    host + ":" + port.toString
  }
}



