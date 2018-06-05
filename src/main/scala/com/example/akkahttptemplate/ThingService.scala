package com.example.akkahttptemplate

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.example.akkahttptemplate.exceptions.HttpException
import scala.collection.concurrent.{Map, TrieMap}
import scala.concurrent.{Future, blocking}
import scala.util.Try

// Service class.  Separates the service logic from the HTTP wrapper to ease
// unit testing.
object ThingService {

  // Shared mutable storage.
  // If it's small amounts of data, you can also just use actors instead but
  // this is a demo of "actor-less" usage.
  private val thingStorage: Map[String, Thing] = TrieMap.empty 

  // Execution context.  TODO customize this.
  import scala.concurrent.ExecutionContext.Implicits.global

  // Testing
  def workV(millis: Long): Long = { println(s"$millis: Sleeping for $millis..."); Thread.sleep(millis); if (millis==666) throw new RuntimeException("Some error happened!"); println(s"$millis: Done sleeping for $millis."); millis }
  // With blocking:
  def workVB(millis: Long): Long = { blocking{ workV(millis) } }

  /**
   * Get a thing by id.
   */
  def get(id: String): Future[Option[Thing]] = Future {

    // For any storage (typically database access, you will need to add this
    // blocking flag to tell the executor that this may block and it's ok.
    // A TrieMap is supposed to be lock free, so this is just for demonstration.
    blocking {
      // Uncomment the below to test how blocking the endpoint works.
      //workV(Try{id.toLong}.getOrElse(2000))
      thingStorage.get(id)
    }
  }

  /**
   * Get all the things!
   */
  def getAll: Future[Seq[Thing]] = Future {

    // Add blocking flag to all code that may block (even though this doesn't).
    blocking {
      thingStorage.values.toSeq
    }
  }

  /**
    * Post a new (or updated) thing
    */
  def post(thing: Thing): Future[WriteResult[Thing]] = Future {
    blocking {
      def save() = thingStorage += thing.id->thing

      if (thingStorage.get(thing.id).nonEmpty) {
        save()
        WriteResult[Thing](StatusCodes.OK, "Successfully updated thing.  Id is "+thing.id, Option(thing))
      }
      else {
        save()
        WriteResult[Thing](StatusCodes.Created, "Successfully created thing.  Id is "+thing.id, Option(thing))
      }
    }
  }

  /**
    * Put a new (or updated) thing. In this app, there is no 
    * difference between PUT and POST. The ids must match though.
    */
  def put(id: String, thing: Thing): Future[WriteResult[Thing]] = { 
    val check = Future {
      if (id != thing.id) throw new HttpException(StatusCodes.BadRequest, "Bad request!  Must PUT to the same path as the 'id' field.")
    }

    for {
      c <- check
      p <- post(thing)
    } yield p
  }

  /**
    * Delete a thing. 
    */
  def delete(id: String): Future[WriteResult[Thing]] = Future {
    blocking {
      thingStorage -= id
      WriteResult[Thing](StatusCodes.NoContent, "", None)
    }
  }

  // Helper methods for testing
  // TODO this needs to be a class not object; these aren't properly protected.
  def getStorage: scala.collection.immutable.Map[String, Thing] = thingStorage.toMap
  protected[akkahttptemplate] def clearStorage = thingStorage.clear
}

// Returned from most write methods:
case class WriteResult[T](statusCode: StatusCode, message: String, thing: Option[T])

