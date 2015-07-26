package pt.org.apec.services.books.test.db

import org.scalatest._

import pt.org.apec.services.books.db._

import spray.http._

class PublicationsSpec extends FlatSpec with BaseRouteSpec with Matchers with BasicData {
  
  "Publications API" should "Return an empty list when no publications exists" in {
    Get("/publications") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Seq[PublicationInfo]] should have size 0
    }
  }
  
  it should "Create new publications" in {
    createMemorial
  }
  
  def createMemorial : PublicationInfo = {
    val request = NewPublicationRequest("Memorial do Convento", "memorial-do-convento", Seq(createSaramago.guid), Seq(createLiteratura.guid), Some(1978))
    Post("/publications", request) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.Created
      responseAs[PublicationInfo].authors.map(_.guid) should contain(request.authors.head)
      responseAs[PublicationInfo]
    }
  }
  
  it should "Get publications after created" in {
    val memorial = createMemorial
    Get("/publications") ~> sealRoute(routes) ~> check {
      responseAs[Seq[PublicationInfo]] should have size 1
    }
    Get("/publications/" + memorial.guid.toString) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[PublicationInfo].guid shouldBe memorial.guid
    }
  }
}
