package pt.org.apec.services.books.db

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.joda.time.DateTime
import scala.concurrent.Future
import org.postgresql.util.PSQLException
import pt.org.apec.services.books.common._

/**
 * @author ragb
 */
class PublicationsStore(db: Database)(implicit executorContext: ExecutionContext) {
  def createSchema = db.run(Tables.schema.create)
  def dropSchema = db.run(Tables.schema.drop)
  def createCategory(category: NewCategoryRequest): Future[Category] = db.run(Queries.insertCategory(Category(createGUID, category.name, category.slug)))
    .recoverWith(mapDuplicateException)
  def getCategories: Future[Seq[Category]] = db.run(Queries.listCategories.result)
  def getCategoryBySlug(slug: String): Future[Option[Category]] = db.run(Queries.getCategoryBySlug(slug).result.headOption)
  def getAuthors: Future[Seq[Author]] = db.run(Queries.listAuthors.result)
  def createAuthor(newAuthor: NewAuthorRequest): Future[Author] = db.run(Queries.insertAuthor(Author(createGUID, newAuthor.name, newAuthor.slug))).recoverWith(mapDuplicateException)
  def getAuthorBySlug(slug: String): Future[Option[Author]] = db.run(Queries.getAuthorBySlug(slug).result.headOption)
  def getAuthorByGUID(guid: UUID): Future[Option[Author]] = db.run(Queries.getAuthorByGUID(guid).result.headOption)

  def createPublicationStatus(newPublicationStatus: NewPublicationStatusRequest): Future[PublicationStatus] = db.run(Queries.insertPublicationStatus(PublicationStatus(createGUID, newPublicationStatus.name, newPublicationStatus.slug, newPublicationStatus.score))).recoverWith(mapDuplicateException)
  def getPublicationStatuses: Future[Seq[PublicationStatus]] = db.run(Queries.listPublicationStatuses.result)
  def getPublicationStatusBySlug(slug: String): Future[Option[PublicationStatus]] = db.run(Queries.getPublicationStatusBySlug(slug).result.headOption)

  def createPublication(newPublication: NewPublicationRequest): Future[PublicationInfo] = {
    val guid = createGUID
    val action = (for {
      p <- Queries.insertPublication(Publication(guid, newPublication.title, newPublication.slug, newPublication.publicationYear, DateTime.now(), None, newPublication.notes, newPublication.publicationStatusGUID))
      _ <- Queries.insertPublicationAuthors(newPublication.authors)(p)
      _ <- Queries.insertPublicationCategories(newPublication.categories)(p)
      s <- newPublication.publicationStatusGUID
        .map { guid => Queries.getPublicationStatusByGUID(guid).result.headOption }
        .getOrElse(DBIO.successful(None))
      result <- mkPublicationInfo(p, s)
    } yield (result)).transactionally
    db.run(action) recoverWith (mapDuplicateException)
  }

  def getPublications: Future[Seq[PublicationInfo]] = {
    val q = for {
      (p, s) <- Queries.getPublications joinLeft Tables.publicationStatuses on (_.publicationStatusGUID === _.guid)
    } yield (p, s)
    val publications = q.result
    val actions = publications flatMap { ps =>
      val infos: Seq[DBIO[PublicationInfo]] = ps map { case (p, s) => mkPublicationInfo(p, s) }
      DBIO.sequence(infos)
    }
    db.run(actions)
  }

  def getPublicationByGUID(guid: UUID): Future[Option[PublicationInfo]] = {
    // This gets confusing using a for compreension
    val q = for {
      (p, s) <- Tables.publications joinLeft Tables.publicationStatuses on (_.publicationStatusGUID === _.guid) if p.guid === guid
    } yield (p, s)

    val action = q.result.headOption
      .flatMap {
        case Some((p, s)) => mkPublicationInfo(p, s).map(Some.apply)
        case _ => DBIO.successful(None)
      }
    db.run(action)
  }

  private def mkPublicationInfo(publication: Publication, status: Option[PublicationStatus]): DBIO[PublicationInfo] = {
    val authors = Queries.getPublicationAuthors(publication.guid).result
    val categories = Queries.getPublicationCategories(publication.guid).result
    (authors zip categories) map {
      case (a, c) =>
        PublicationInfo(publication.guid, a, c, publication.title, publication.slug, publication.publicationYear, publication.createdAt, publication.updatedAt, publication.notes, status)
    }
  }
  private val mapDuplicateException: PartialFunction[Throwable, Future[Nothing]] = {
    case e: PSQLException if e.getSQLState == "23505" => {
      Future.failed(new DuplicateFound())
    }
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
    def insertPublicationStatus(publicationStatus: PublicationStatus) = (publicationStatuses returning publicationStatuses.map(_.guid) into ((p, guid) => p)) += publicationStatus
    val getPublicationStatusBySlug = publicationStatuses.findBy(_.slug)
    val getPublicationStatusByGUID = publicationStatuses.findBy(_.guid)
    val listPublicationStatuses = publicationStatuses.sortBy(_.score)
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

case class DatabaseException(errorCode: String) extends Throwable
class DuplicateFound extends DatabaseException("error.duplicateFound")