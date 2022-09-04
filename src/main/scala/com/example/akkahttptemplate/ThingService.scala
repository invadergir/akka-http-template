package com.example.akkahttptemplate

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import com.example.akkahttptemplate.exceptions.HttpException
import scala.collection.concurrent.{Map, TrieMap}
import scala.concurrent.{Future, blocking}
import scala.util.Try

// Things Service trait.  
// Separates the service logic from the HTTP wrapper to ease unit testing.

trait ThingService {

  /**
   * Get a thing by id.
   */
  def get(id: String): Future[Option[Thing]]

  /**
   * Get all the things!
   */
  def getAll(): Future[Seq[Thing]]

  /**
    * Post a new (or updated) thing
    */
  def post(thing: Thing): Future[WriteResult[Thing]]

  /**
    * Put a new (or updated) thing. In this app, there is no 
    * difference between PUT and POST. The ids must match though.
    */
  def put(id: String, thing: Thing): Future[WriteResult[Thing]] 

  /**
    * Delete a thing. 
    */
  def delete(id: String): Future[WriteResult[Thing]]

  // temp testing
  //def transform(i: Int): Int = i + 2
}

// Real implementation.
// If you want to initialize it with a set of things (for example, for testing),
// you can do it here.
class ThingServiceImpl(
  initialDataSet: Thing*, 
) extends ThingService {

  // Shared mutable storage (thread safe).
  // A real app will use actors or a database.
  private val thingStorage = TrieMap.empty[String, Thing]

  // Insert all the initial data, if any.
  initialDataSet.foreach{ thing =>
    thingStorage += thing.id->thing
  }

  // Execution context.  TODO: customize this.
  import scala.concurrent.ExecutionContext.Implicits.global

  // Testing
  def work(millis: Long): Long = { println(s"$millis: Sleeping for $millis..."); Thread.sleep(millis); if (millis==666) throw new RuntimeException("Some error happened!"); println(s"$millis: Done sleeping for $millis."); millis }
  // With blocking:
  def workB(millis: Long): Long = { blocking{ work(millis) } }

  /**
   * Get a thing by id.
   */
  override def get(id: String): Future[Option[Thing]] = Future {

    // For any storage (typically database access, you will need to add this
    // blocking flag to tell the executor that this may block and it's ok.
    blocking {
      // Uncomment the below to test how blocking the endpoint works.
      //work(Try{id.toLong}.getOrElse(2000))
      thingStorage.get(id)
    }
  }

  /**
   * Get all the things!
   */
  override def getAll(): Future[Seq[Thing]] = Future {

    // Add blocking flag to all code that may block (even though this doesn't).
    blocking {
      thingStorage.values.toSeq
    }
  }

  /**
    * Post a new (or updated) thing
    */
  override def post(thing: Thing): Future[WriteResult[Thing]] = Future {
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
  override def put(id: String, thing: Thing): Future[WriteResult[Thing]] = { 
    val check = Future {
      if (id != thing.id) throw new HttpException(StatusCodes.BadRequest, "Bad request!  Must PUT to the same path as the 'id' field.")
    }

    for {
      _ <- check
      p <- post(thing)
    } yield p
  }

  /**
    * Delete a thing. 
    */
  override def delete(id: String): Future[WriteResult[Thing]] = Future {
    blocking {
      thingStorage -= id
      WriteResult[Thing](StatusCodes.NoContent, "", None)
    }
  }

  // Helper method for testing.  This copies the data to an immutable map to return.
  def getStorage: scala.collection.immutable.Map[String, Thing] = thingStorage.toMap
}

// Returned from most write methods:
case class WriteResult[T](statusCode: StatusCode, message: String, thing: Option[T])

