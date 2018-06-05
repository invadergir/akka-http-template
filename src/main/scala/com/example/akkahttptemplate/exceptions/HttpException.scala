package com.example.akkahttptemplate.exceptions

import akka.http.scaladsl.model.StatusCode

class HttpException(val code: StatusCode, val message: String)
  extends Exception(message: String) 

