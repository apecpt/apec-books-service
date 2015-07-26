package pt.org.apec.services.books.db

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.joda.time.DateTime
import scala.concurrent.Future
import org.postgresql.util.PSQLException

/**
 * @author ragb
 */
class PublicationsStore(db: Database)(implicit executorContext: ExecutionContext) {
  def createSchema = db.run(Tables.schema.create)
  def dropSchema = db.run(Tables.schema.drop)
  def createCategory(category: NewCategoryRequest): Future[Category] = db.run(Queries.insertCategory(Category(createGUID, category.slug)))
    .recoverWith(mapDuplicateException)
  def getCategories: Future[Seq[Category]] = db.run(Queries.listCategories.result)
  def getCategoryBySlug(slug: String): Future[Option[Category]] = db.run(Queries.getCategoryBySlug(slug).result.headOption)
  def getAuthors: Future[Seq[Author]] = db.run(Queries.listAuthors.result)
  def createAuthor(newAuthor: NewAuthorRequest): Future[Author] = db.run(Queries.insertAuthor(Author(createGUID, newAuthor.name, newAuthor.slug))).recoverWith(mapDuplicateException)
  def getAuthorBySlug(slug: String): Future[Option[Author]] = db.run(Queries.getAuthorBySlug(slug).result.headOption)
  def getAuthorByGUID(guid: UUID): Future[Option[Author]] = db.run(Queries.getAuthorByGUID(guid).result.headOption)

  def createPublication(newPublication: NewPublicationRequest): Future[PublicationInfo] = {
    val guid = createGUID
    val action = (for {
      p <- Queries.insertPublication(Publication(guid, newPublication.title, newPublication.slug, newPublication.publicationYear, DateTime.now(), None, newPublication.notes))
      _ <- Queries.insertPublicationAuthors(newPublication.authors)(p)
      _ <- Queries.insertPublicationCategories(newPublication.categories)(p)
      result <- mkPublicationInfo(p)
    } yield (result)).transactionally
    db.run(action) recoverWith (mapDuplicateException)
  }

  def getPublications: Future[Seq[PublicationInfo]] = {
    val publications = Queries.getPublications.result
    val actions = publications flatMap { ps =>
      val infos: Seq[DBIO[PublicationInfo]] = ps map mkPublicationInfo
      DBIO.sequence(infos)
    }
    db.run(actions)
  }

  def getPublicationByGUID(guid: UUID): Future[Option[PublicationInfo]] = {
    // This gets confusing using a for compreension
    val action = Queries.getPublicationByGUID(guid).result.headOption
    .flatMap {_.map(mkPublicationInfo(_).map(Some.apply)).getOrElse(DBIO.successful(None))}
    db.run(action)
  }

  private def mkPublicationInfo(publication: Publication): DBIO[PublicationInfo] = {
    val authors = Queries.getPublicationAuthors(publication.guid).result
    val categories = Queries.getPublicationCategories(publication.guid).result
    (authors zip categories) map {
      case (a, c) =>
        PublicationInfo(publication.guid, a, c, publication.title, publication.slug, publication.publicationYear, publication.createdAt, publication.updatedAt, publication.notes)
    }
  }
  private val mapDuplicateException: PartialFunction[Throwable, Future[Nothing]] = {
    case e: PSQLException if e.getSQLState == "23505" => Future.failed(new DuplicateFound())
  }

  private def createGUID = UUID.randomUUID()

  object Queries {
    import Tables._

    def insertCategory(category: Category) = (categories returning categories.map(_.guid) into ((c, guid) => c)) += category
    def listCategories = categories
    val getCategoryByGUID = categories.findBy(_.guid)
    val getCategoryBySlug = categories.findBy(_.slug)
    val listAuthors = authors
    def insertAuthor(author: Author) = (authors returning authors.map(_.guid) into ((a, guid) => a)) += author
    val getAuthorBySlug = authors.findBy(_.slug)
    val getAuthorByGUID = authors.findBy(_.guid)
    def insertPublication(publication: Publication) = (publications returning publications.map(_.guid) into ((p, guid) => p)) += publication
    def insertPublicationAuthors(authorGUIDs: Seq[UUID])(publication: Publication) = publicationAuthors ++= authorGUIDs.map(guid => (guid, publication.guid))
    def insertPublicationCategories(categoryGUIDs: Seq[UUID])(publication: Publication) = publicationCategories ++= categoryGUIDs.map(guid => (guid, publication.guid))
    val getPublicationByGUID = publications.findBy(_.guid)
    def getPublicationAuthors(publicationGUID: UUID) = for {
      (_, author) <- publicationAuthors.filter(_.publicationGUID === publicationGUID) join authors on (_.authorGUID === _.guid)
    } yield (author)
    def getPublicationCategories(publicationGUID: UUID) = for {
      (_, category) <- publicationCategories.filter(_.publicationGUID === publicationGUID) join categories on (_.categoryGUID === _.guid)
    } yield (category)
    val getPublications = publications
  }

}

case class NewCategoryRequest(slug: String) {
  require(slug.nonEmpty, "Slug must not be empty")
}

case class NewAuthorRequest(name: String, slug: String) {
  require(slug.nonEmpty, "Slug must not be empty")
  require(name.nonEmpty, "Name must not be empty")
}

case class PublicationInfo(guid: UUID, authors: Seq[Author], categories: Seq[Category], title: String, slug: String, publicationYear: Option[Int], createdAt: DateTime, updatedAt: Option[DateTime], notes: Option[String])
case class NewPublicationRequest(title: String, slug: String, authors: Seq[UUID], categories: Seq[UUID], publicationYear: Option[Int], notes: Option[String] = None) {
  require(authors.nonEmpty, "Authors must not be empty")
  require(categories.nonEmpty, "Categories must not be empty")
  require(slug.nonEmpty, "Slug must not be empty")
  require(title.nonEmpty, "Title must not be empty")
}

case class DatabaseException(errorCode: String) extends Throwable
class DuplicateFound extends DatabaseException("error.duplicateFound")