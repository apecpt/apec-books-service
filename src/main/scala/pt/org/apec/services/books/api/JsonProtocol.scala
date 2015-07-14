package pt.org.apec.services.books.api

import play.api.libs.json._
import spray.httpx.PlayJsonSupport

import pt.org.apec.services.books.db._

trait JsonProtocol extends PlayJsonSupport {
  implicit val newCategoryRequestFormat = Json.format[NewCategoryRequest]
  implicit val categoryFormat = Json.format[Category]
  implicit val DuplicateFoundWrites = Json.writes[DatabaseException]
  implicit val newAuthorRequestFormat = Json.format[NewAuthorRequest]
  implicit val authorFormat = Json.format[Author]
}
