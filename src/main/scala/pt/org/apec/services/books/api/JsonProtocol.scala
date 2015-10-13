package pt.org.apec.services.books.api

import play.api.libs.json._
import play.api.libs.functional.syntax._
import spray.httpx.PlayJsonSupport
import pt.org.apec.services.books.common._
import pt.org.apec.services.books.common.json._
import pt.org.apec.services.books.db._

trait JsonProtocol extends PlayJsonSupport with JsonFormaters {
  implicit val DuplicateFoundWrites = Json.writes[DatabaseException]
  import ImportDataController._
  implicit val rawPublicationFormat = Json.format[RawPublication]
  implicit val importDataResultFormat = Json.format[ImportDataResult]
  implicit val categoryWithCountsWrites = new Writes[(Category, Int)] {
    def writes(t: (Category, Int)) = {
      categoryFormat.writes(t._1) + ("publicationCount" -> Json.toJson(t._2))
    }
  }

  implicit val categoryWithCountsReads = ((JsPath()).read[Category] ~
    (JsPath \ "publicationCount").read[Int])
    .tupled
}

