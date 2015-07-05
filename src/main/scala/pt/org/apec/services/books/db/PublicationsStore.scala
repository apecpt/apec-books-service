package pt.org.apec.services.books.db

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate
import scala.concurrent.Future

/**
 * @author ragb
 */
class PublicationsStore(db : Database)(implicit executorContext : ExecutionContext) {
  def createSchema = db.run(Tables.schema.create)
  def dropSchema = db.run(Tables.schema.drop)
  def createCategory(category : NewCategoryRequest) : Future[Category] = db.run(Actions.insertCategory(Category(createGUID, category.name)))
  def getCategories : Future[Seq[Category]] = db.run(Actions.listCategories)
  def getCategoryBySlug(slug : String) : Future[Option[Category]] = db.run(Actions.getCategoryBySlug(slug))
  private def createGUID = UUID.randomUUID()
  
  object Actions {
    import Tables._
    
    def insertCategory(category : Category) = (categories returning categories.map(_.guid) into ((c, guid) => c)) += category
    def listCategories = categories.result
    def getCategoryByGUID(guid : UUID) = categories.filter(_.guid === guid).result.headOption
    def getCategoryBySlug(slug : String) = categories.filter(_.slug === slug).result.headOption
  }
}

case class NewCategoryRequest(name : String)
case class PublicationInfo(guid : UUID, authors : Seq[Author], categories : Seq[Category], title : String, publicationYear : Option[Int], createdAt : LocalDateTime, updatedAt : Option[LocalDateTime], notes : Option[String])