package pt.org.apec.services.books.api

import spray.routing._
import spray.http._
import akka.actor.Actor
import pt.org.apec.services.books.db._
import pt.org.apec.services.books.common._
import spray.http._
import scala.concurrent.ExecutionContext
import godiva.spray.pagination.PaginationDirectives
import godiva.json.PlayJsonProtocol
import scala.util.control.NonFatal
import akka.event.Logging
import akka.event.LoggingAdapter
import spray.util.LoggingContext
import scala.concurrent.Future

class BooksServiceActor(val publicationsStore: PublicationsStore) extends Actor with BooksService {
  def actorRefFactory = context
  implicit override val executionContext = context.dispatcher
  def receive = runRoute(routes)
}

trait BooksService extends HttpService with JsonProtocol with PaginationDirectives with PublicationOrderDirectives {
  def publicationsStore: PublicationsStore
  implicit val executionContext: ExecutionContext

  implicit def exceptionHandler(implicit log: LoggingContext) = ExceptionHandler {
    case e: DuplicateFound => complete {
      StatusCodes.Conflict -> e
    }
    case NonFatal(e) => complete {
      log.error(e, "Unforesiable error processing request")
      StatusCodes.InternalServerError -> e
    }
  }

  def routes = (logRequestResponse(("routes", Logging.DebugLevel)) & handleExceptions(exceptionHandler)) { categoryRoutes ~ authorRoutes ~ publicationRoutes ~ publicationStatusesRoutes ~ importRoutes }

  def categoryRoutes = pathPrefix("categories") {
    pathEndOrSingleSlash {
      get {
        parameters("counts".as[Boolean].?(false)) { counts =>
          if (counts) {
            complete(publicationsStore.getCategoryCounts)
          } else {
            complete(publicationsStore.getCategories)
          }
        }
      } ~
        (post & entity(as[NewCategoryRequest])) { newCategory =>
          complete {
            StatusCodes.Created -> publicationsStore.createCategory(newCategory)
          }
        }
    } ~
      path(Segment) { slug =>
        (get & rejectEmptyResponse) {
          complete {
            publicationsStore.getCategoryBySlug(slug)
          }
        }
      }

  }

  def authorRoutes = pathPrefix("authors") {
    pathEndOrSingleSlash {
      get {
        complete {
          publicationsStore.getAuthors
        }
      } ~
        (post & entity(as[NewAuthorRequest])) { newAuthor =>
          complete {
            StatusCodes.Created -> publicationsStore.createAuthor(newAuthor)
          }
        }
    } ~
      path(Segment) { slug =>
        (get & rejectEmptyResponse) {
          complete {
            publicationsStore.getAuthorBySlug(slug)
          }
        }
      }
  }

  def publicationRoutes = pathPrefix("publications") {
    pathEndOrSingleSlash {
      get {
        paginated { paginationRequest =>
          publicationsSorted { order =>
            parameters("q".?) { maybeQ =>
              maybeQ map { q =>
                complete {
                  publicationsStore.searchPublications(q, paginationRequest)
                }
              } getOrElse {
                complete {
                  publicationsStore.getPublications(paginationRequest, order)
                }
              }
            }
          }
        }
      } ~
        (post & entity(as[NewPublicationRequest])) { newPublication =>
          complete {
            StatusCodes.Created -> publicationsStore.createPublication(newPublication)
          }
        }
    } ~
      pathPrefix(Segment) { slug =>
        pathEndOrSingleSlash {
          (get & rejectEmptyResponse) {
            complete {
              publicationsStore.getPublicationBySlug(slug)
            }
          }
        } ~
          pathPrefix("files") {
            pathEndOrSingleSlash {
              (get & rejectEmptyResponse) {
                import scalaz._, Scalaz._
                complete {
                  (for {
                    guid <- OptionT(publicationsStore.getPublicationGUIDFromSlug(slug))
                    files <- OptionT(publicationsStore.getPublicationFiles(guid).map(_.some))
                  } yield (files)).run
                }
              } ~
                (post & entity(as[NewPublicationFileRequest])) { request =>
                  complete {
                    import scalaz._, Scalaz._
                    (for {
                      guid <- OptionT(publicationsStore.getPublicationGUIDFromSlug(slug))
                      result <- OptionT(publicationsStore.createPublicationFile(guid, request).map(_.some))
                    } yield (StatusCodes.Created -> result)).run
                  }
                }
            }
          }
      }
  }

  def publicationStatusesRoutes = pathPrefix("publicationStatuses") {
    pathEndOrSingleSlash {
      get {
        complete {
          publicationsStore.getPublicationStatuses
        }
      } ~
        (post & entity(as[NewPublicationStatusRequest])) { publicationStatus =>
          complete {
            StatusCodes.Created -> publicationsStore.createPublicationStatus(publicationStatus)
          }
        }
    }
  }

  def importRoutes = {
    import ImportDataController._
    val importDataController = new ImportDataController(publicationsStore)
    pathPrefix("import" / "rawPublications") {
      (post & entity(as[Seq[RawPublication]])) { publications =>
        complete {
          importDataController.importRawPublications(ImportRawPublicationsRequest(publications))
        }
      }
    }
  }
}
