package pt.org.apec.services.books.api

import spray.routing._
import spray.http._
import akka.actor.Actor
import pt.org.apec.services.books.db._
import spray.json._
import java.util.UUID
import spray.http._
import spray.httpx.SprayJsonSupport._

trait JsonProtocol extends DefaultJsonProtocol {
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(value: UUID) = JsString(value toString)

    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case _           => deserializationError("expecting UUID")
    }
  }

  implicit def formatNewCategoryRequest = jsonFormat1(NewCategoryRequest.apply)
  implicit def formatCategory = jsonFormat2(Category.apply)
}


class BooksServiceActor(val publicationsStore : PublicationsStore) extends Actor with BooksService {
  def actorRefFactory = context
  
  def receive = runRoute(routes)
}


trait BooksService extends HttpService with JsonProtocol {
  def publicationsStore : PublicationsStore
  
  def routes = categoryRoutes
  
  def categoryRoutes = path("categories") {
    get {
      complete(StatusCodes.OK -> publicationsStore.getCategories)
            } ~
    (post & entity(as[NewCategoryRequest])) { newCategory =>
      complete{
        StatusCodes.Created -> publicationsStore.createCategory(newCategory)
      }
      }
  }
}