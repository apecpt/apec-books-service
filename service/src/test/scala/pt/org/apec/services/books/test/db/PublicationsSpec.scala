package pt.org.apec.services.books.test.db

import org.scalatest._

import pt.org.apec.services.books.db._
import pt.org.apec.services.books.common._

import spray.http._
import spray.httpx.unmarshalling._
import spray.httpx.PlayJsonSupport._
import godiva.spray.json.PlayJsonProtocol
import godiva.core.pagination._
import pt.org.apec.services.books.common.json.JsonFormaters

class PublicationsSpec extends FlatSpec with BaseRouteSpec with Matchers with BasicData {
  "Publications API" should "Return an empty list when no publications exists" in {
    Get("/publications") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[PaginatedResult[PublicationInfo]].elements should have size 0
    }
  }

  it should "Create new publications" in {
    createMemorial
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

  it should "Get publications after created" in {
    val memorial = createMemorial
    Get("/publications") ~> sealRoute(routes) ~> check {
      responseAs[PaginatedResult[PublicationInfo]].elements should have size 1
    }
    Get("/publications/" + memorial.guid.toString) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[PublicationInfo].guid shouldBe memorial.guid
      responseAs[PublicationInfo].publicationStatus shouldBe defined
    }
  }

  it should "Get publication counts for empty categories" in {
    val literatura = createLiteratura
    Get(s"/categories?counts=true") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      val r = responseAs[Seq[WithPublicationCount[Category]]]
      r should have size 1
      r.head.publicationCount shouldBe 0
    }
  }

  it should "Get publication countss when books exist" in {
    val memorial = createMemorial
    val categoryGuid = memorial.categories(0).guid
    Get("/categories?counts=true") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      val r = responseAs[Seq[WithPublicationCount[Category]]]
      r should have size 1
      r(0).publicationCount shouldBe 1
    }
  }
}
