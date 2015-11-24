package pt.org.apec.services.books.test.db

import org.scalatest._
import pt.org.apec.services.books.db._
import spray.http.StatusCodes
import pt.org.apec.services.books.common._

trait BasicData extends Matchers {
  this: BaseRouteSpec =>

  def createLiteratura = {
    Post("/categories", NewCategoryRequest("literatura", "literatura")) ~> routes ~> check {
      status shouldBe StatusCodes.Created
      responseAs[Category]
    }
  }

  def createSaramago = {
    Post("/authors", NewAuthorRequest("Jos Saramago", "jose-saramago")) ~> routes ~> check {
      status shouldBe StatusCodes.Created
      responseAs[Author]
    }
  }

  def createCorrigido = {
    Post("/publicationStatuses", NewPublicationStatusRequest("Corrigido", "corrigido", 0)) ~> routes ~> check {
      status shouldBe StatusCodes.Created
      responseAs[PublicationStatus]
    }
  }
  def createMemorial: PublicationInfo = {
    val request = NewPublicationRequest("Memorial do Convento", "memorial-do-convento", Seq(createSaramago.guid), Seq(createLiteratura.guid), Some(1978), publicationStatusGUID = Some(createCorrigido.guid))
    Post("/publications", request) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.Created
      responseAs[PublicationInfo].authors.map(_.guid) should contain(request.authors.head)
      responseAs[PublicationInfo].publicationStatus shouldBe defined
      responseAs[PublicationInfo]
    }
  }

}