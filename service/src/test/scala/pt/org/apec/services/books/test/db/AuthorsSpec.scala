package pt.org.apec.services.books.test.db

import org.scalatest._
import pt.org.apec.services.books.db._
import spray.http._
import pt.org.apec.services.books.common._

class AuthorsSpec extends BaseRouteSpec with Matchers {

  val rui = NewAuthorRequest("Rui Batista", "rui-batista")

  def createRui() {
    Post("/authors", rui) ~> routes ~> check {
      status shouldBe StatusCodes.Created
    }
  }

  "Authors API" should "Allow Insertion of new Authors" in {
    createRui
  }

  it should "get created authors" in {
    createRui
    Get("/authors") ~> routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Seq[Author]] should have size 1
    }
  }

  it should "Get created authors by slug" in {
    createRui
    Get("/authors/" + rui.slug) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Author].slug shouldBe rui.slug
    }
  }

  it should "return 404 not found for non existent authors" in {
    Get("/authors/godiva") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }
}