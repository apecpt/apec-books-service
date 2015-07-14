package pt.org.apec.services.books.api

import spray.routing._
import spray.http._
import akka.actor.Actor
import pt.org.apec.services.books.db._
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
  def routes = categoryRoutes

  def categoryRoutes = pathPrefix("categories") {
    pathEnd {
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
      
    
}
