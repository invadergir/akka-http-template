package com.example.akkahttptemplate.specs

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.akkahttptemplate._
import org.json4s.jackson.Serialization._

class ThingRoutesSpec
  extends UnitSpec
  with ScalatestRouteTest
  with JsonSupport
{

  var route = Server.thingRoutes
  implicit val jsonFormats = org.json4s.DefaultFormats

  // before all tests have run
  override def beforeAll() = {
    super.beforeAll()
  }

  // before each test has run
  override def beforeEach() = {
    super.beforeEach()
    ThingService.clearStorage
  }

  // after each test has run
  override def afterEach() = {
    //myAfterEach()
    super.afterEach()
  }

  // after all tests have run
  override def afterAll() = {
    super.afterAll()
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Tests start
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // How to use akka-http-testkit: 
  // https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html
  describe("POST /things") {

    // Examples from web:
    //it("should return a greeting for GET requests to the root path") {
    //  Get() ~> smallRoute ~> check {
    //    responseAs[String] shouldEqual "Captain on the bridge!"
    //  }
    //}
    //
    //"return a 'PONG!' response for GET requests to /ping" in {
    //  Get("/ping") ~> smallRoute ~> check {
    //    responseAs[String] shouldEqual "PONG!"
    //  }
    //}
    //
    //"leave GET requests to other paths unhandled" in {
    //  Get("/kermit") ~> smallRoute ~> check {
    //    handled shouldBe false
    //  }
    //}
    //
    //"return a MethodNotAllowed error for PUT requests to the root path" in {
    //  Put() ~> Route.seal(smallRoute) ~> check {
    //    status shouldEqual StatusCodes.MethodNotAllowed
    //    responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
    //  }
    //}
    //Post("/", HttpEntity(`application/json`, """{ "name": "Jane", "favoriteNumber" : 42 }""")) ~>
    //  route ~> check {
    //    responseAs[String] shouldEqual "Person: Jane - favorite number: 42"
    //  }


    it("should return Created with a link to the newly created thing") {
      val entity = Thing("A", "AAA")
      Post(
        "/things",
        HttpEntity(`application/json`, write(entity))
      ) ~> route ~> check {
        status shouldEqual StatusCodes.Created
        val strResponse = responseAs[String]
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true
        //println(s"strResponse=$strResponse")
        val response = read[RestPostResult](strResponse)
        response.link shouldBe "/things/A"
      }
    }

    it("should return OK if it already exists") {
      Post(
        "/things", 
        HttpEntity(`application/json`, write(Thing("A", "AAA")))
      ) ~> route ~> check {
        // first time, created
        status shouldEqual StatusCodes.Created
      }

      // 2nd time, ok
      Post(
        "/things",
        HttpEntity(`application/json`, write(Thing("A", "AAA2")))
      ) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }
}

  
