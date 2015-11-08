package pt.org.apec.services.books.api

import play.api.libs.json._
import play.api.libs.functional._
import play.api.libs.functional.syntax._
import spray.httpx.PlayJsonSupport
import pt.org.apec.services.books.common._
import pt.org.apec.services.books.common.json._
import pt.org.apec.services.books.db._
import godiva.core.pagination._
import godiva.json.PlayJsonProtocol

trait JsonProtocol extends PlayJsonSupport with JsonFormaters with PlayJsonProtocol {
  implicit val throwableWrites = new Writes[Throwable] {
    def writes(t: Throwable) = JsObject(Map("type" -> JsString(t.getClass.getName), "message" -> JsString(t.getMessage)))
  }
  implicit val DuplicateFoundWrites = Json.writes[DatabaseException]
  import ImportDataController._
  implicit val rawPublicationFormat = Json.format[RawPublication]
  implicit val importDataResultFormat = Json.format[ImportDataResult]
  implicit val publicationREsultReads = paginatedResultReads[PublicationInfo]

  // preety print
  implicit val printer = Json.prettyPrint _
}

