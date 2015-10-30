package pt.org.apec.services.books.db

import slick.driver.PostgresDriver
import java.util.UUID
import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import pt.org.apec.services.books.common._
import godiva.slick.DriverComponent
import godiva.slick.TablesSchema

trait TablesComponent extends TablesSchema {
  this: DriverComponent[PostgresDriver] =>
  import driver.api._
  class Authors(tag: Tag) extends Table[Author](tag, "books") {
    def guid = column[UUID]("guid", O.PrimaryKey)
    def name = column[String]("name")
    def slug = column[String]("slug", O.Length(256))
    def slugIndex = index("author_slug_idx", slug, true)
    def * = (guid, name, slug) <> (Author.tupled, Author.unapply _)
  }

  object Authors {
    val authors = TableQuery[Authors]
  }

  class Categories(tag: Tag) extends Table[Category](tag, "categories") {
    val guid = column[UUID]("guid", O.PrimaryKey)
    def name = column[String]("name")
    def slug = column[String]("slug", O.Length(48))
    def slugIndex = index("category_slug_idx", slug, true)
    def * = (guid, name, slug) <> (Category.tupled, Category.unapply _)
  }

  class PublicationStatuses(tag: Tag) extends Table[PublicationStatus](tag, "publication_statuses") {
    def guid = column[UUID]("guid", O.PrimaryKey)
    def name = column[String]("name")
    def slug = column[String]("slug", O.Length(256))
    def slugIndex = index("publication_status_slug_idx", slug, true)
    def score = column[Int]("score")
    def * = (guid, name, slug, score) <> (PublicationStatus.tupled, PublicationStatus.unapply _)
  }

  class Publications(tag: Tag) extends Table[Publication](tag, "publications") {
    def guid = column[UUID]("guid", O.PrimaryKey)
    def title = column[String]("title", O.Length(1024))
    def slug = column[String]("slug", O.Length(256))
    def publicationYear = column[Option[Int]]("publication_year", O.Length(4))
    def createdAt = column[DateTime]("created_at")
    def updatedAt = column[Option[DateTime]]("updated_at")
    def notes = column[Option[String]]("notes", O.Length(1024))
    def slugIndex = index("publication_slug_idx", slug, true)
    def publicationStatusGUID = column[Option[UUID]]("status_guid")
    def publicationStatus = foreignKey("publication_status_fk", publicationStatusGUID, publicationStatuses)(_.guid.?)
    def * = (guid, title, slug, publicationYear, createdAt, updatedAt, notes, publicationStatusGUID) <> (Publication.tupled, Publication.unapply _)
  }

  class PublicationAuthors(tag: Tag) extends Table[(UUID, UUID)](tag, "publication_author") {
    def authorGUID = column[UUID]("author_GUID")
    def publicationGUID = column[UUID]("publication_GUID")
    def author = foreignKey("author_fk", authorGUID, Authors.authors)(_.guid)
    def publication = foreignKey("publication_fk", publicationGUID, publications)(_.guid)
    def publicationAuthorIndex = index("publication_author_idx", (authorGUID, publicationGUID), true)
    def * = (authorGUID, publicationGUID)
  }

  class PublicationCategories(tag: Tag) extends Table[(UUID, UUID)](tag, "publication_category") {
    def categoryGUID = column[UUID]("category_guid")
    def publicationGUID = column[UUID]("publication_guid")
    def categoryPublicationIndex = index("category_publication_idx", (categoryGUID, publicationGUID), true)
    def publication = foreignKey("publication_fk", publicationGUID, publications)(_.guid)
    def category = foreignKey("category_fk", categoryGUID, categories)(_.guid)
    def * = (categoryGUID, publicationGUID)
  }
  val authors = TableQuery[Authors]
  val publications = TableQuery[Publications]
  val categories = TableQuery[Categories]
  val publicationAuthors = TableQuery[PublicationAuthors]
  val publicationCategories = TableQuery[PublicationCategories]
  val publicationStatuses = TableQuery[PublicationStatuses]
  override def tables = Seq(authors, publications, categories, publicationAuthors, publicationCategories, publicationStatuses)
}

