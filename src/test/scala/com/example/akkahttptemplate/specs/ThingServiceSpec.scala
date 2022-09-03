package com.example.akkahttptemplate.specs

import com.example.akkahttptemplate._
import akka.http.scaladsl.model.StatusCodes

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ThingServiceSpec
  extends UnitSpec
{

  val initialThings = Seq(
    Thing("A", "AAA"),
    Thing("B", "BBB"),
    Thing("C", "CCC"),
  )
  val initialThingsMap = initialThings.map{ t => (t.id, t) }.toMap

  // Var so we can reset it before each test.
  var testService: ThingServiceImpl = null

  // Before all tests have run
  override def beforeAll() = {
    super.beforeAll()
  }

  // Before each test has run:
  override def beforeEach() = {
    super.beforeEach()
    // Create the service and populate with initial Things.
    testService = new ThingServiceImpl(initialThings :_*)
  }

  // after each test has run
  override def afterEach() = {
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

  // Helper function to block and get the future result.
  // By default we assume the future is very fast.
  def blockFor[T](f: Future[T], timeout: Duration = 1 seconds): T = {
    Await.result(f, timeout)
  }

  // GET all the things
  describe("getAll()") {
    it("should respond with the initial list of things") {
      val actualThings = blockFor(testService.getAll)
      actualThings.toSet shouldBe initialThings.toSet
    }
  }

  describe("get()") {
    it("should return the thing asked for") {
      blockFor(testService.get("B")) shouldBe Some(initialThingsMap("B"))
    }
    it("should not return the thing asked for if it's not there") {
      blockFor(testService.get("NON_EXISTENT")) shouldBe None
      blockFor(testService.get("")) shouldBe None
    }
  }

  describe("post()") {

    // This ties our raw service to the HTTP 'statuscode', but is an acceptable
    // tradeoff to get more knowledge about failures baked into the actual service.
    // If we ever change the comm layer, say to gRPC, we could translate the
    // status code into an appropriate return type in the RPC layer.
    it("should return a successful WriteResult for the thing posted.") {
      val thing = Thing("M", "MMM")
      val result = blockFor(testService.post(thing))
      result.statusCode shouldBe StatusCodes.Created
      result.message.nonEmpty shouldBe true
      result.thing shouldBe Some(thing)
    }

    it("should return OK if it already exists") {
      val id = "M"
      val thing = Thing(id, "MMM")
      // 1st time, created
      blockFor(testService.post(thing)).statusCode shouldBe StatusCodes.Created
      // 2nd time, same id, different value, ok (does an update)
      val thing2 = Thing(id, "MMM222")
      blockFor(testService.post(thing2)).statusCode shouldBe StatusCodes.OK
    }
  }

  describe("put()") {

    it("should return Created with a link to the newly created thing") {
      val thing = Thing("M", "MMM")
      val result = blockFor(testService.put(thing.id, thing))
      result.statusCode shouldBe StatusCodes.Created
      result.message.nonEmpty shouldBe true
      result.thing shouldBe Some(thing)
    }

    it("should return OK if it already exists, similarly to post()") {
      val id = "M"
      val thing = Thing(id, "MMM")
      // 1st time, created
      blockFor(testService.put(id, thing)).statusCode shouldBe StatusCodes.Created
      // 2nd time, same id, different value, ok (does an update)
      val thing2 = Thing(id, "MMM222")
      blockFor(testService.post(thing2)).statusCode shouldBe StatusCodes.OK
    }
  }

  describe("delete()") {

    it("should return NoContent always, if it exists or not") {
      val id = "A"
      // exists:
      blockFor(testService.get(id)).nonEmpty shouldBe true
      blockFor(testService.delete(id)).statusCode shouldBe StatusCodes.NoContent
      // not exists:
      blockFor(testService.get(id)).isEmpty shouldBe true
      blockFor(testService.delete(id)).statusCode shouldBe StatusCodes.NoContent
      blockFor(testService.delete("NON_EXISTENT")).statusCode shouldBe StatusCodes.NoContent
      blockFor(testService.delete("")).statusCode shouldBe StatusCodes.NoContent
    }
  }
}

  
