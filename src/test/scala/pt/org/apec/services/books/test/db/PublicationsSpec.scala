package pt.org.apec.services.books.test.db

import org.scalatest._

import pt.org.apec.services.books.db._

import spray.http._

class PublicationsSpec extends FlatSpec with BaseRouteSpec with Matchers with BasicData {
  
  "Publications API" should "Return and empty list when no publications exists" in {
    Get("/publications") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Seq[PublicationInfo]] should have size 0
      
    }
  }
}
