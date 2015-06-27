package pt.org.apec.services.books.test.db

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import pt.org.apec.services.books.db._

/**
 * @author ragb
 */
class CategoriesSpec extends FlatSpec with DatabaseSpec with Matchers {
  
  "Publications store" should "insert new categories" in {
    val category = NewCategoryRequest("test")
    publicationsStore.createCategory(category).futureValue.slug shouldBe(category.name)
  }
}