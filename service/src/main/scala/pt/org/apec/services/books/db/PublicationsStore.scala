package pt.org.apec.services.books.db

import godiva.slick._
import slick.driver.PostgresDriver
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.joda.time.DateTime
import scala.concurrent.Future
import org.postgresql.util.PSQLException
import pt.org.apec.services.books.common._
import pt.org.apec.services.books.common.PublicationSorting._
import godiva.core.pagination.PaginationRequest
import godiva.core.pagination.PaginatedResult
import com.github.tototoshi.slick.PostgresJodaSupport._
import com.github.tminglei.slickpg.TsVector

/**
 * @author ragb
 */
trait PublicationsStore extends SchemaManagement with TablesSchema with TablesComponent with Pagination {
  this: DriverComponent[CustomPostgresDriver] with DatabaseComponent[PostgresDriver] with DefaultExecutionContext =>
  import driver.api._
  import MaybeUtils.MaybeExtensionMethods
  override def tables = super[TablesComponent].tables
  def createCategory(category: NewCategoryRequest): Future[Category] = database.run(Queries.insertCategory(Category(createGUID, category.name, category.slug)))
    .recoverWith(mapDuplicateException)
  def getCategories: Future[Seq[Category]] = database.run(Queries.listCategories.result)
  def getCategoryBySlug(slug: String): Future[Option[Category]] = database.run(Queries.getCategoryBySlug(slug).result.headOption)
  def getAuthors(query: Option[String] = None): Future[Seq[Author]] = database.run(Queries.listAuthors(query).result)
  def createAuthor(newAuthor: NewAuthorRequest): Future[Author] = database.run(Queries.insertAuthor(Author(createGUID, newAuthor.name, newAuthor.slug))).recoverWith(mapDuplicateException)
  def getAuthorBySlug(slug: String): Future[Option[Author]] = database.run(Queries.getAuthorBySlug(slug).result.headOption)
  def getAuthorByGUID(guid: UUID): Future[Option[Author]] = database.run(Queries.getAuthorByGUID(guid).result.headOption)

  def createPublicationStatus(newPublicationStatus: NewPublicationStatusRequest): Future[PublicationStatus] = database.run(Queries.insertPublicationStatus(PublicationStatus(createGUID, newPublicationStatus.name, newPublicationStatus.slug, newPublicationStatus.score))).recoverWith(mapDuplicateException)
  def getPublicationStatuses: Future[Seq[PublicationStatus]] = database.run(Queries.listPublicationStatuses.result)
  def getPublicationStatusBySlug(slug: String): Future[Option[PublicationStatus]] = database.run(Queries.getPublicationStatusBySlug(slug).result.headOption)

  def createPublication(newPublication: NewPublicationRequest): Future[PublicationInfo] = {
    val guid = createGUID
    val action = (for {
      p <- Queries.insertPublication(Publication(guid, newPublication.title, newPublication.slug, newPublication.publicationYear, DateTime.now(), None, newPublication.notes, newPublication.publicationStatusGUID))
      _ <- Queries.insertPublicationAuthors(newPublication.authors)(p)
      _ <- Queries.insertPublicationCategories(newPublication.categories)(p)
      s <- newPublication.publicationStatusGUID
        .map { guid => Queries.getPublicationStatusByGUID(guid).result.headOption }
        .getOrElse(DBIO.successful(None))
      _ <- Queries.insertPublicationSearch(p.guid, p.title, "portuguese")
      result <- mkPublicationInfo(p, s)
    } yield (result)).transactionally
    database.run(action) recoverWith (mapDuplicateException)
  }

  def getPublications(paginationRequest: PaginationRequest, order: PublicationOrder = PublicationOrder(CreatedAt, Desc), filters: PublicationFilters = PublicationFilters()): Future[PaginatedResult[PublicationInfo]] = {
    val q = for {
      (p, s) <- Queries.getPublications(filters) joinLeft publicationStatuses on (_.publicationStatusGUID === _.guid)
    } yield (p, s)
    // TODO: use the direction attribute.
    val sortedQ = order.attribute match {
      case `Title` => q.sortBy(_._1.title.asc)
      case `CreatedAt` => q.sortBy(_._1.createdAt.desc)
      case `UpdatedAt` => q.sortBy(_._1.updatedAt.desc)
    }
    val publications = sortedQ.paginated(paginationRequest)
    val actions = publications flatMap {
      case PaginatedResult(ps, page, totals) =>
        val infos: Seq[DBIO[PublicationInfo]] = ps map { case (p, s) => mkPublicationInfo(p, s) }
        DBIO.sequence(infos).map(PaginatedResult(_, page, totals))
    }
    database.run(actions)
  }
  def searchPublications(query: String, pagination: PaginationRequest) = {
    val q = (for {
      ((p, (_, _, rank, _)), s) <- Queries.getPublications(PublicationFilters()) join publicationSearches.map(p => (p.publicationGUID, p.vector, tsRank(p.vector, toTsQuery(query, Some("portuguese"))), p.languageConfig)).filter(p => p._2 @@ toTsQuery(query, Some("portuguese"))) on (_.guid === _._1) joinLeft publicationStatuses on (_._1.publicationStatusGUID === _.guid)
    } yield (p, s, rank)).sortBy(_._3)
    val publications = q.paginated(pagination)
    val actions = publications flatMap {
      case PaginatedResult(ps, page, totals) =>
        val infos: Seq[DBIO[PublicationInfo]] = ps map { case (p, s, _) => mkPublicationInfo(p, s) }
        DBIO.sequence(infos).map(PaginatedResult(_, page, totals))
    }
    database.run(actions)

  }

  def getPublicationGUIDFromSlug(slug: String): Future[Option[UUID]] = database.run(publications.filter(_.slug === slug).map(_.guid).result.headOption)
  def getPublicationByGUID(guid: UUID): Future[Option[PublicationInfo]] = {
    // This gets confusing using a for compreension
    val q = for {
      (p, s) <- publications joinLeft publicationStatuses on (_.publicationStatusGUID === _.guid) if p.guid === guid
    } yield (p, s)

    val action = q.result.headOption
      .flatMap {
        case Some((p, s)) => mkPublicationInfo(p, s).map(Some.apply)
        case _ => DBIO.successful(None)
      }
    database.run(action)
  }

  def getPublicationBySlug(slug: String): Future[Option[PublicationInfo]] = {
    // This gets confusing using a for compreension
    val q = for {
      (p, s) <- publications joinLeft publicationStatuses on (_.publicationStatusGUID === _.guid) if p.slug === slug
    } yield (p, s)

    val action = q.result.headOption
      .flatMap {
        case Some((p, s)) => mkPublicationInfo(p, s).map(Some.apply)
        case _ => DBIO.successful(None)
      }
    database.run(action)
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

  def createPublicationFile(publicationGUID: UUID, request: NewPublicationFileRequest): Future[PublicationFile] = database.run(Queries.insertPublicationFile(publicationGUID, request)).recoverWith(mapDuplicateException)
  def getPublicationFiles(publicationGUID: UUID): Future[Seq[PublicationFile]] = database.run(Queries.getPublicationFiles(publicationGUID).result)

  def getCategoryCounts: Future[Seq[WithPublicationCount[Category]]] = database.run(Queries.getCategoryCounts.result.map(s => s.map(t => WithPublicationCount(t._1, t._2))))

  private def createGUID = UUID.randomUUID()

  object Queries {

    def insertCategory(category: Category) = (categories returning categories.map(_.guid) into ((c, guid) => c)) += category
    def listCategories = categories
    val getCategoryByGUID = categories.findBy(_.guid)
    val getCategoryBySlug = categories.findBy(_.slug)
    def listAuthors(query: Option[String] = None) = query map { query =>
      authorsComplete.filter(_.vector @@ toTsQuery(query, Some("pg_catalog.portuguese"))).sortBy(t => tsRank(t.vector, toTsQuery(query, Some("pg_catalog.portuguese")))).map(_.forSelect)
    } getOrElse (authors)
    def insertAuthor(author: Author) = (authors returning authorsComplete.map(_.guid) into ((a, guid) => a)) += author
    val getAuthorBySlug = (slug: String) => authorsComplete.filter(_.slug === slug).map(_.forSelect)
    val getAuthorByGUID = (guid: UUID) => authorsComplete.filter(_.guid === guid).map(_.forSelect)
    def insertPublicationStatus(publicationStatus: PublicationStatus) = (publicationStatuses returning publicationStatuses.map(_.guid) into ((p, guid) => p)) += publicationStatus
    val getPublicationStatusBySlug = publicationStatuses.findBy(_.slug)
    val getPublicationStatusByGUID = publicationStatuses.findBy(_.guid)
    val listPublicationStatuses = publicationStatuses.sortBy(_.score)
    def insertPublication(publication: Publication) = (publications returning publications.map(_.guid) into ((p, guid) => p)) += publication
    def insertPublicationAuthors(authorGUIDs: Seq[UUID])(publication: Publication) = publicationAuthors ++= authorGUIDs.map(guid => (guid, publication.guid))
    def insertPublicationCategories(categoryGUIDs: Seq[UUID])(publication: Publication) = publicationCategories ++= categoryGUIDs.map(guid => (guid, publication.guid))
    val getPublicationByGUID = publications.findBy(_.guid)
    def getPublicationAuthors(publicationGUID: UUID) = for {
      (_, a) <- publicationAuthors.filter(_.publicationGUID === publicationGUID) join authorsComplete on (_.authorGUID === _.guid)
    } yield (a.forSelect)
    def getPublicationCategories(publicationGUID: UUID) = for {
      (_, category) <- publicationCategories.filter(_.publicationGUID === publicationGUID) join categories on (_.categoryGUID === _.guid)
    } yield (category)
    def getPublications(filters: PublicationFilters) = publications
      .maybeFilter(filters.authorGUID) { authorGUID => t => publicationAuthors.filter(pa => pa.authorGUID === authorGUID && pa.publicationGUID === t.guid).exists }
      .maybeFilter(filters.categoryGUID) { categoryGUID => t => publicationCategories.filter(pc => pc.categoryGUID === categoryGUID && pc.publicationGUID === t.guid).exists }
      .maybeFilter(filters.publicationStatusGUID) { statusGUID => t => t.publicationStatusGUID === statusGUID }

    def getPublicationFiles(publicationGUID: UUID) = publicationFiles.filter(_.publicationGUID === publicationGUID).filter(_.available === true)
    def insertPublicationFile(publicationGUID: UUID, request: NewPublicationFileRequest) = (publicationFiles returning publicationFiles.map(_.guid) into ((p, guid) => p)) += PublicationFile(createGUID, publicationGUID, request.name, request.contentType, request.size, request.url)

    def insertPublicationSearch(publicationGUID: UUID, title: String, language: String) = {
      // slick sql's interpulator sucks for collections.
      val names = sql"select coalesce(string_agg(a.name, ' '), '') from authors as a where a.guid in (select p.author_guid from publication_authors as p where p.publication_guid = ${publicationGUID.toString}::uuid)".as[String].head
      names flatMap { n =>
        publicationSearches.forceInsertExpr((publicationGUID, toTsVector(title, Some(language)).setWeight('a') @+ toTsVector(n, Some(language)).setWeight('d'), language))
      }

    }

    def getCategoryCounts = {
      val q = (for {
        (c, pcs) <- listCategories joinLeft publicationCategories on (_.guid === _.categoryGUID)
      } yield (c, pcs)).groupBy(_._1)
      val q2 = q map {
        case (c, p) => (c, p.map(_._2.map(_.publicationGUID)).countDistinct)
      }
      q2
    }

  }

}

class DatabaseException(val errorCode: String, message: String) extends Exception(message)
class DuplicateFound extends DatabaseException("error.duplicateFound", "Entry already exists")

case class PublicationFilters(authorGUID: Option[UUID] = None, categoryGUID: Option[UUID] = None, publicationStatusGUID: Option[UUID] = None)