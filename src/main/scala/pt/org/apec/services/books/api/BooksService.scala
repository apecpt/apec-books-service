package pt.org.apec.services.books.api

import spray.routing._
import spray.http._
import akka.actor.Actor
import pt.org.apec.services.books.db._
import pt.org.apec.services.books.common._
import spray.http._
import spray.httpx.PlayJsonSupport._
import scala.concurrent.ExecutionContext

class BooksServiceActor(val publicationsStore: PublicationsStore) extends Actor with BooksService {
  def actorRefFactory = context
  implicit override val executionContext = context.dispatcher
  def receive = runRoute(routes)
}

trait BooksService extends HttpService with JsonProtocol {
  def publicationsStore: PublicationsStore
  implicit val executionContext: ExecutionContext

  implicit def exceptionHandler = ExceptionHandler {
    case e: DuplicateFound => complete { StatusCodes.Conflict -> e }
  }

  def routes = categoryRoutes ~ authorRoutes ~ publicationRoutes

  def categoryRoutes = pathPrefix("categories") {
    pathEndOrSingleSlash {
      get {
        complete(publicationsStore.getCategories)
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
        complete {
          publicationsStore.getPublications
        }
      } ~
        (post & entity(as[NewPublicationRequest])) { newPublication =>
          complete {
            StatusCodes.Created -> publicationsStore.createPublication(newPublication)
          }
        }
    } ~
      path(JavaUUID) { guid =>
        (get & rejectEmptyResponse) {
          complete {
            publicationsStore.getPublicationByGUID(guid)
          }
        }
      }
  }
}
