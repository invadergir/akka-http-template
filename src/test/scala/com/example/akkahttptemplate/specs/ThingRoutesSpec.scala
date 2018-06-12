package com.example.akkahttptemplate.specs

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.akkahttptemplate._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.jackson.Serialization._

class ThingRoutesSpec
  extends UnitSpec
  with ScalatestRouteTest
  with Json4sSupport
{

  var route = Server.thingRoutes
  implicit val jsonFormats = org.json4s.DefaultFormats

  val initialThings = Seq(
    Thing("A", "AAA"),
    Thing("B", "BBB"),
    Thing("C", "CCC"),
  )

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

  // Helper method to post initialThings before tests that get them.
  def postInitialThings(): Unit = {
    initialThings.foreach{ thing =>
      Post(
        "/things",
        HttpEntity(`application/json`, write(thing))
      ) ~> route
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Tests start
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // How to use akka-http-testkit:
  // https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html

  // Note: in a real application, you test only the HTTP stuff here,
  // and leave service logic to the service unit tests.

  // GET all the things
  describe("GET /things") {
    it("should respond with an array of things, OK, json content-type") {
      postInitialThings()
      Get("/things") ~> Route.seal(route) ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = responseAs[String]
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true

        val response = read[Seq[Thing]](strResponse)
        response.toSet shouldBe initialThings.toSet
      }
    }
  }

  describe("GET /things/B") {
    it("should respond thing B, OK, json content") {
      postInitialThings()
      Get("/things/B") ~> Route.seal(route) ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = responseAs[String]
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true

        val response = read[Thing](strResponse)
        response shouldBe initialThings.find( _.id == "B" ).get
      }
    }
  }

  describe("POST /things") {

    it("should return Created with a link to the newly created thing") {
      val entity = Thing("A", "AAA")
      Post(
        "/things",
        HttpEntity(`application/json`, write(entity))
      ) ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.Created
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = responseAs[String]
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true
        val response = read[RestPostResult](strResponse)
        response.link shouldBe "/things/A"
      }
    }

    it("should return OK if it already exists") {
      // 1st time, created
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

  describe("PUT /things/{}") {

    it("should return Created with a link to the newly created thing") {
      val entity = Thing("A", "AAA")
      Put(
        "/things/A",
        HttpEntity(`application/json`, write(entity))
      ) ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.Created
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = responseAs[String]
        Option(strResponse) shouldNot be(None)
        strResponse.nonEmpty shouldBe true
        val response = read[RestPostResult](strResponse)
        response.link shouldBe "/things/A"
      }
    }

    it("should return OK if it already exists") {
      // 1st time, created
      Put(
        "/things/A",
        HttpEntity(`application/json`, write(Thing("A", "AAA")))
      ) ~> route ~> check {
        // first time, created
        status shouldEqual StatusCodes.Created
      }

      // 2nd time, ok
      Put(
        "/things/A",
        HttpEntity(`application/json`, write(Thing("A", "AAA2")))
      ) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }

  describe("DELETE /things/{}") {

    it("should return OK") {
      postInitialThings()
      Delete("/things/B") ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.NoContent
        responseAs[String].isEmpty shouldBe true
      }
    }
  }

  // Unknown routes.
  // Note 'handled' only works with "~> route" not with "Route.seal(route)".
  describe("unknown routes") {
    it("GET /XXX should be unhandled") {
      Get("/XXX") ~> route ~> check {
        handled shouldBe false
      }
      Get("/XXX") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
    it("GET /thing/A should be unhandled - collections are plural") {
      Get("/thing/A") ~> route ~> check {
        handled shouldBe false
      }
      Get("/thing/A") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }
}

  
