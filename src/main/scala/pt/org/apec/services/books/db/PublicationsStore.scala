package pt.org.apec.services.books.db

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate
import scala.concurrent.Future
import org.postgresql.util.PSQLException

/**
 * @author ragb
 */
class PublicationsStore(db: Database)(implicit executorContext: ExecutionContext) {
  def createSchema = db.run(Tables.schema.create)
  def dropSchema = db.run(Tables.schema.drop)
  def createCategory(category: NewCategoryRequest): Future[Category] = db.run(Actions.insertCategory(Category(createGUID, category.slug)))
    .recoverWith(mapDuplicateException)
  def getCategories: Future[Seq[Category]] = db.run(Actions.listCategories)
  def getCategoryBySlug(slug: String): Future[Option[Category]] = db.run(Actions.getCategoryBySlug(slug))
  def getAuthors : Future[Seq[Author]]= db.run(Actions.listAuthors)
  def createAuthor(newAuthor: NewAuthorRequest) : Future[Author] = db.run(Actions.insertAuthor(Author(createGUID, newAuthor.name, newAuthor.slug))).recoverWith(mapDuplicateException)
  def getAuthorBySlug(slug : String) : Future[Option[Author]]= db.run(Actions.getAuthorBySlug(slug))

  private val mapDuplicateException: PartialFunction[Throwable, Future[Nothing]] = {
    case e: PSQLException if e.getSQLState == "23505" => Future.failed(new DuplicateFound())
  }

  private def createGUID = UUID.randomUUID()

  object Actions {
    import Tables._

    def insertCategory(category: Category) = (categories returning categories.map(_.guid) into ((c, guid) => c)) += category
    def listCategories = categories.result
    def getCategoryByGUID(guid: UUID) = categories.filter(_.guid === guid).result.headOption
    def getCategoryBySlug(slug: String) = categories.filter(_.slug === slug).result.headOption
    def listAuthors = authors.result
    def insertAuthor(author: Author) = (authors returning authors.map(_.guid) into ((a, guid) => a)) += author
    def getAuthorBySlug(slug : String) = authors.filter(_.slug === slug).result.headOption
  }
}

case class NewCategoryRequest(slug: String)
case class NewAuthorRequest(name: String, slug: String)
case class PublicationInfo(guid: UUID, authors: Seq[Author], categories: Seq[Category], title: String, publicationYear: Option[Int], createdAt: LocalDateTime, updatedAt: Option[LocalDateTime], notes: Option[String])

case class DatabaseException(errorCode: String) extends Throwable
class DuplicateFound extends DatabaseException("error.duplicateFound")