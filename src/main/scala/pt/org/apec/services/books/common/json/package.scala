package pt.org.apec.services.books.common

import play.api.libs.json._

import pt.org.apec.services.books.common._

/**
 * @author ragb
 */
package object json {
  trait JsonFormaters {
  import JodaConverters._
  implicit val newCategoryRequestFormat = Json.format[NewCategoryRequest]
  implicit val categoryFormat = Json.format[Category]
  implicit val newAuthorRequestFormat = Json.format[NewAuthorRequest]
  implicit val authorFormat = Json.format[Author]
  implicit val newPublicationRequestFormat = Json.format[NewPublicationRequest]
  implicit val publicationInfoFormat = Json.format[PublicationInfo]
  
}

object JsonFormaters extends JsonFormaters


}