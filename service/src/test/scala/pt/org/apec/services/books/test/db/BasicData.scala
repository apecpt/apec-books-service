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
}