package pt.org.apec.services.books.test.db

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.IntegrationPatience
import spray.testkit.ScalatestRouteTest
import pt.org.apec.services.books.api.BooksService
import spray.http._
import pt.org.apec.services.books.common._

/**
 * @author ragb
 */
class CategoriesSpec extends FlatSpec with BaseRouteSpec with Matchers {

  "Publications API" should "insert new categories" in {
    val category = NewCategoryRequest("test", "teste")
    Post("/categories", category) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.Created
      responseAs[Category].slug shouldBe category.slug
    }
  }

  it should "insert new categories with diferent GUIDs" in {
    val c1 = NewCategoryRequest("c1", "c1")
    var c1Response: Category = null
    Post("/categories", c1) ~> routes ~> check {
      status shouldBe StatusCodes.Created
      c1Response = responseAs[Category]
    }
    val c2 = NewCategoryRequest("c2", "c2")
    Post("/categories", c2) ~> routes ~> check {
      status shouldBe StatusCodes.Created
      c1Response.guid shouldNot be(responseAs[Category].guid)
    }
  }

  it should "Get inserted categories by slug" in {
    val c1 = NewCategoryRequest("c1", "c1")
    Post("/categories", c1) ~> routes ~> check {
      status shouldBe StatusCodes.Created
    }
    Get("/categories/" + c1.slug) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Category].slug shouldBe c1.slug
    }
  }

  it should "return 404 for non existent categories" in {
    Get("/Categories/bubu") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }

  it should "not allow two categories with the same slug" in {
    val category = NewCategoryRequest("c1", "c1")
    Post("/categories", category) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.Created
    }
    Post("/categories", category) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.Conflict
    }

  }
}
