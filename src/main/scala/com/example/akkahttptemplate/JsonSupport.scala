package com.example.akkahttptemplate

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {

  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val thingJsonFormat = jsonFormat2(Thing)
  implicit val restPostResultFormat = jsonFormat2(RestPostResult)
  implicit val restPutResultFormat = jsonFormat2(RestPutResult)
}

