package com.example.akkahttptemplate

import pureconfig._
import pureconfig.generic.auto._
import pureconfig.generic.ProductHint

// app-wide settings / configuration object
object Config {

  implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  //implicit val myEnumExampleType = ConfigReader.fromString[MyEnumExample.Value](
  //  ConvertHelpers.catchReadError(s => MyEnumExample.withName(s)))
  //implicit val converterOffsetDateTime = offsetDateTimeConfigConvert(DateTimeFormatter.ISO_DATE_TIME)
  //implicit val converterlocalDateTime = localDateTimeConfigConvert(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  // global app configuration
  val app: AppConfig = ConfigSource.default.loadOrThrow[AppConfigWrapper].app

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

// "AppConfigWrapper" is the top-level config object, and it's only necessary in
// order to provide an enclosing object.  Previous pureconfig APIs allowed you
// to do that without an extra wrapping case class.
case class AppConfigWrapper(app: AppConfig)
case class AppConfig(thisServer: ServerConnection) {

  // TODO add custom configs here.  Put validation code in case class constructors
  // (OR add cats validation)
}

case class ServerConnection(
  host: String,
  port: Int,
  protocol: String = "http",
) {

  // Simple check on the connection parameters. 
  check(host.nonEmpty && port > 0 && protocol.nonEmpty, 
    "ServerConnection instance is invalid: "+this.toString)

  /** Get the host and port for use in connection strings.
    * 
    */
  val hostPort: String = {
    host + ":" + port.toString
  }
}



