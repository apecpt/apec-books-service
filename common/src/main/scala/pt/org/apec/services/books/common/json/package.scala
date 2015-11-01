package pt.org.apec.services.books.common.json

import play.api.libs.json._
import play.api.libs.functional.syntax._

import pt.org.apec.services.books.common._

/**
 * @author ragb
 */
trait JsonFormaters {
  import JodaConverters._
  implicit val newCategoryRequestFormat = Json.format[NewCategoryRequest]
  implicit val categoryFormat = Json.format[Category]
  implicit val newAuthorRequestFormat = Json.format[NewAuthorRequest]
  implicit val authorFormat = Json.format[Author]
  implicit val newPublicationRequestFormat = Json.format[NewPublicationRequest]
  implicit val newPublicationStatusRequestFormat = Json.format[NewPublicationStatusRequest]
  implicit val publicationStatusFormat = Json.format[PublicationStatus]
  implicit val publicationInfoFormat = Json.format[PublicationInfo]
  implicit def withPublicationCountWrites[T: Writes] = new Writes[WithPublicationCount[T]] {
    def writes(e: WithPublicationCount[T]) = {
      implicitly[Writes[T]].writes(e.element).asInstanceOf[JsObject] + ("publicationCount" -> Json.toJson(e.publicationCount))
    }
  }

  implicit def withPublicationCountReads[T: Reads]: Reads[WithPublicationCount[T]] = ((JsPath().read[T] ~ (JsPath \ "publicationCount").read[Int])).tupled.map(t => WithPublicationCount[T](t._1, t._2))

}

object JsonFormaters extends JsonFormaters
