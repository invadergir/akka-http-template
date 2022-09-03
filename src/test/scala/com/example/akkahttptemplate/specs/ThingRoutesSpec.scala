package com.example.akkahttptemplate.specs

import akka.actor.ActorSystem
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.util.FastFuture.EnhancedFuture
import com.example.akkahttptemplate.{WriteResult, _}
import org.scalatest.OneInstancePerTest

import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory

import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import scala.language.postfixOps

class MockServer()
  extends ThingRoutes
    with MockFactory
    with OneInstancePerTest {

  //implicit val system: ActorSystem = ActorSystem("MockServer-ActorSystem")

  val thingService: ThingService = stub[ThingService]

  //def transform(i: Int) = thingService.transform(i)
}


class ThingRoutesSpec
  extends UnitSpec
  with MockFactory
  with ScalatestRouteTest
{
  import FailFastCirceSupport._
  import io.circe.generic.auto._

  val initialThings = Seq(
    Thing("A", "AAA"),
    Thing("B", "BBB"),
    Thing("C", "CCC"),
  )
  val initialThingsMap = initialThings.map{ t => (t.id, t) }.toMap

  // Vars set up before every test:
  var mockServer = new MockServer()
  var route = mockServer.thingRoutes

  // before all tests have run
  override def beforeAll() = {
    super.beforeAll()
  }

  // before each test has run
  override def beforeEach() = {
    super.beforeEach()
    mockServer = new MockServer()
    route = mockServer.thingRoutes
  }

  // after each test has run
  override def afterEach() = {
    super.afterEach()
  }

  // after all tests have run
  override def afterAll() = {
    super.afterAll()
  }

  // Hack to help get the string out of the response.
  // There is an issue related to this test kit and circe; responseAs[String] doesn't work properly.  (TODO?)
  def getResponseString(response: HttpResponse): String = {
    response.entity.asInstanceOf[HttpEntity.Strict].data.decodeString("UTF-8")
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

  describe("GET /things") {
    it("should respond with an array of things, OK, json content-type") {
      (mockServer.thingService.getAll _).when().returns(Future(initialThings))
      
      Get("/things") ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        // val strResponse = responseAs[String] // this doesn't work (TODO???)
        val strResponse = getResponseString(response)
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true

        val things = decode[Seq[Thing]](strResponse).toOption.get
        things.toSet shouldBe initialThings.toSet
      }
    }
  }

  describe("GET /things/B when B exists") {
    it("should respond thing B, OK, json content") {
      (mockServer.thingService.get _).when("B").returns(
        Future(Some(initialThingsMap("B"))))

      Get("/things/B") ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = getResponseString(response)
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true

        val thing = decode[Thing](strResponse).toOption.get
        thing shouldBe initialThingsMap("B")
      }
    }
  }

  describe("GET /things/B when B does not exist") {
    it("should respond with NotFound for unknown Things") {
      (mockServer.thingService.get _).when("B").returns(Future(None))
      (mockServer.thingService.get _).when("").returns(Future(None))

      Get("/things/B") ~> Route.seal(route) ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.NotFound
      }

      Get("/things/") ~> Route.seal(route) ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.NotFound
      }
    }
  }

  describe("POST /things") {

    it("should return Created with a link to the newly created thing") {
      val thingA = initialThingsMap("A")
      (mockServer.thingService.post _).when(thingA).returns(
        Future(WriteResult(StatusCodes.Created, "nonEmpty message", Option(thingA)))
      )

      Post(
        "/things",
        HttpEntity(`application/json`, thingA.asJson.noSpaces)
      ) ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.Created
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = getResponseString(response)
        Option(strResponse) shouldNot be (None)
        strResponse.nonEmpty shouldBe true
        val result = decode[RestPostResult](strResponse).toOption.get
        result.link shouldBe "/things/A"
      }
    }

    it("should return OK if it already exists") {
      // while the mock is creating this status code, it is still valuable
      // to ensure that the code passes through the http layer safely.
      val thingA = initialThingsMap("A")
      // 1st time, created
      (mockServer.thingService.post _).when(thingA).returns(
        Future(WriteResult(StatusCodes.Created, "nonEmpty message", Option(thingA)))
      )

      Post(
        "/things", 
        HttpEntity(`application/json`, thingA.asJson.noSpaces)
      ) ~> route ~> check {
        // first time, created
        status shouldEqual StatusCodes.Created
      }

      // 2nd time, ok
      val thingA2 = thingA.copy(description = "AAA222")
      (mockServer.thingService.post _).when(thingA2).returns(
        Future(WriteResult(StatusCodes.OK, "nonEmpty message", Option(thingA2)))
      )
      Post(
        "/things",
        HttpEntity(`application/json`, thingA2.asJson.noSpaces)
      ) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

  }

  describe("PUT /things/{}") {

    it("should return Created with a link to the newly created thing") {
      val thingA = initialThingsMap("A")
      (mockServer.thingService.put _).when(thingA.id, thingA).returns(
        Future(WriteResult(StatusCodes.Created, "nonEmpty message", Option(thingA)))
      )
      Put(
        "/things/A",
        HttpEntity(`application/json`, thingA.asJson.noSpaces)
      ) ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.Created
        contentType shouldBe ContentTypes.`application/json`

        val strResponse = getResponseString(response)
        Option(strResponse) shouldNot be(None)
        strResponse.nonEmpty shouldBe true
        val result = decode[RestPostResult](strResponse).toOption.get
        result.link shouldBe "/things/A"
      }
    }

    it("should return OK if it already exists") {
      // while the mock is creating this status code, it is still valuable
      // to ensure that the code passes through the http layer safely.
      val thingA = initialThingsMap("A")
      // 1st time, created
      (mockServer.thingService.put _).when(thingA.id, thingA).returns(
        Future(WriteResult(StatusCodes.Created, "nonEmpty message", Option(thingA)))
      )
      Put(
        "/things/A",
        HttpEntity(`application/json`, Thing("A", "AAA").asJson.noSpaces)
      ) ~> route ~> check {
        // first time, created
        status shouldEqual StatusCodes.Created
      }

      // 2nd time, ok
      val thingA2 = thingA.copy(description = "AAA222")
      (mockServer.thingService.put _).when(thingA2.id, thingA2).returns(
        Future(WriteResult(StatusCodes.OK, "nonEmpty message", Option(thingA2)))
      )
      Put(
        "/things/A",
        HttpEntity(`application/json`, thingA2.asJson.noSpaces)
      ) ~> route ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }

  describe("DELETE /things/{}") {

    it("should return OK") {
      // while the mock is creating this status code, it is still valuable
      // to ensure that the code passes through the http layer safely.
      val thingA = initialThingsMap("A")
      (mockServer.thingService.delete _).when(thingA.id).returns(
        Future(WriteResult(StatusCodes.NoContent, "", None))
      )

      Delete("/things/A") ~> route ~> check {
        handled shouldBe true
        status shouldEqual StatusCodes.NoContent
        getResponseString(response).isEmpty shouldBe true
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

  
