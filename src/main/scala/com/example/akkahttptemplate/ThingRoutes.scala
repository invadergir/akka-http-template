package com.example.akkahttptemplate

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.example.akkahttptemplate.exceptions.HttpException
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.jackson
import org.json4s.jackson.Serialization.write

import scala.concurrent.{Future, blocking}

trait ThingRoutes extends Json4sSupport with StrictLogging {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  import Json4sProtocol.formats
  import Json4sSupport._
  implicit val serialization = jackson.Serialization

  // Exception handling - if the service throws a special exception we can 
  // easily map it to HTTP.  Otherwise it gets treated as 500 server error.
  val exceptionTranslator = ExceptionHandler {
    case x: HttpException =>
      extractUri { uri =>
        logger.info(s"${x.message}  Returning ${x.code.toString}.")
        complete(HttpResponse(x.code, entity = x.message))
      }
  }

  // The routes
  lazy val thingRoutes: Route = {

    pathPrefix("things") {
      // This can optionally be added implicitly if you use the same handler
      // for all routes:
      handleExceptions(exceptionTranslator) {

        // Using 'concat' is the same as using '~', but causes less issues.
        concat(

          // For paths that end at the above prefix:
          pathEnd {
            concat(

              // Get all the Things!
              // Note to simply return the array json, we have to return a
              // serialized string manually, hence the json4s write().
              // Otherwise you could create a case class with a sequence inside it,
              // which would be marshalled like normal, but then you have an extra
              // potentially unneeded member inside the json.
              get {
                val listF: Future[Seq[Thing]] = ThingService.getAll
                onSuccess(listF) { list =>
                  complete(list)
                }
              },

              // Post a new Thing
              post {
                entity(as[Thing]) { thing =>
                  val thingCreated: Future[WriteResult[Thing]] = ThingService.post(thing)
                  onSuccess(thingCreated) { pr =>
                    logger.info(pr.message)
                    // How to force a content-type and manually serialize to JSON, not needed
                    // if using Json4sSupport or similar:
//                    complete(HttpResponse(
//                      pr.statusCode,
//                      entity = HttpEntity(
//                        ContentTypes.`application/json`,
//                        write(RestPostResult(pr.message, s"/things/${pr.thing.get.id}")))
//                    ))
                    complete(pr.statusCode, RestPostResult(pr.message, s"/things/${pr.thing.get.id}"))
                  }
                }
              }
            )
          },

          // For paths with a segment following the prefix (ie. the resource id):
          path(Segment) { id =>
            concat(

              // Get one thing.
              get {
                val maybeThing: Future[Option[Thing]] = ThingService.get(id)
                rejectEmptyResponse {
                  complete(maybeThing)
                }
              },

              // Put one thing.
              put {
                entity(as[Thing]) { thing =>
                  val thingCreated: Future[WriteResult[Thing]] = ThingService.put(id, thing)
                  onSuccess(thingCreated) { pr =>
                    logger.info(pr.message)
                    complete((pr.statusCode, RestPutResult(pr.message, s"/things/${pr.thing.get.id}")))
                  }
                }
              },

              // Delete one thing.
              delete {
                val thingDeleted: Future[WriteResult[Thing]] = ThingService.delete(id)
                onSuccess(thingDeleted) { pr =>
                  logger.info(pr.message)
                  complete((pr.statusCode, ""))
                }
              },

            )
          }
        )
      }
    }
  }
}

// Classes that this route returns:
case class RestPostResult(message: String, link: String)
case class RestPutResult(message: String, link: String)

