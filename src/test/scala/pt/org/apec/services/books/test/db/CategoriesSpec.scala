package pt.org.apec.services.books.test.db

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import pt.org.apec.services.books.db._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.IntegrationPatience

/**
 * @author ragb
 */
class CategoriesSpec extends FlatSpec with DatabaseSpec with Matchers with IntegrationPatience {

  "Publications store" should "insert new categories" in {
    val category = NewCategoryRequest("test")
    publicationsStore.createCategory(category).futureValue.slug shouldBe (category.name)
  }

  it should "insert new categories with diferent GUIDs" in {
    val category = NewCategoryRequest("test")
    val r1 = publicationsStore.createCategory(category)
    val category2 = NewCategoryRequest("test2")
    val r2 = publicationsStore.createCategory(category2)
    r1.futureValue.guid shouldNot be(r2.futureValue.guid)
  }
  
  it should "Get inserted categories by slug" in {
    val category = NewCategoryRequest("test")
    val f = for {
      _ <- publicationsStore.createCategory(category)
      r <- publicationsStore.getCategoryBySlug(category.name)
    } yield(r)
    f.futureValue shouldBe defined   
  
  }
}