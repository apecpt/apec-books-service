package pt.org.apec.services.books.test.db

import org.scalatest._
import pt.org.apec.services.books.db._
import spray.http.StatusCodes


trait BasicData extends Matchers {
  this : BaseRouteSpec =>
    
    lazy val literatura = {
      var c : Category = null
      Post("/categories", NewCategoryRequest("literatura")) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        c = responseAs[Category]
      }
      c
    }
    
    lazy val saramago = {
      var author : Author = null
      Post("/authors", NewAuthorRequest("Jos Saramago", "jose-saramago")) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        author = responseAs[Author]
      }
      author
    }
}