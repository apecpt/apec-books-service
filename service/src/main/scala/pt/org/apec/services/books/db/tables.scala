package pt.org.apec.services.books.db

import slick.driver.PostgresDriver
import java.util.UUID
import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import pt.org.apec.services.books.common._
import godiva.slick.DriverComponent
import godiva.slick.TablesSchema
import com.github.tminglei.slickpg._

trait TablesComponent extends TablesSchema {
  this: DriverComponent[CustomPostgresDriver] =>
  import driver.api._
  class Authors(tag: Tag) extends Table[(UUID, String, String, TsVector)](tag, "authors") {
    def guid = column[UUID]("guid", O.PrimaryKey)
    def name = column[String]("name")
    def nameIndex = index("authr_name_idx", name)
    def slug = column[String]("slug", O.Length(256))
    def slugIndex = index("author_slug_idx", slug, true)
    def vector = column[TsVector]("vector")
    def forSelect = (guid, name, slug) <> (Author.tupled, Author.unapply _)
    def * = (guid, name, slug, vector)
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
    def titleIndex = index("publication_title_idx", title, false)
    def createdAtIndex = index("publication_createdat_idx", createdAt, false)
    def publicationStatusGUID = column[Option[UUID]]("status_guid")
    def publicationStatus = foreignKey("publication_status_fk", publicationStatusGUID, publicationStatuses)(_.guid.?)
    def * = (guid, title, slug, publicationYear, createdAt, updatedAt, notes, publicationStatusGUID) <> (Publication.tupled, Publication.unapply _)
  }

  class PublicationAuthors(tag: Tag) extends Table[(UUID, UUID)](tag, "publication_authors") {
    def authorGUID = column[UUID]("author_guid")
    def publicationGUID = column[UUID]("publication_guid")
    def author = foreignKey("author_fk", authorGUID, authorsComplete)(_.guid)
    def publication = foreignKey("publication_fk", publicationGUID, publications)(_.guid)
    def publicationAuthorIndex = index("publication_author_idx", (authorGUID, publicationGUID), true)
    def * = (authorGUID, publicationGUID)
  }

  class PublicationCategories(tag: Tag) extends Table[(UUID, UUID)](tag, "publication_categories") {
    def categoryGUID = column[UUID]("category_guid")
    def publicationGUID = column[UUID]("publication_guid")
    def categoryPublicationIndex = index("category_publication_idx", (categoryGUID, publicationGUID), true)
    def publication = foreignKey("publication_fk", publicationGUID, publications)(_.guid)
    def category = foreignKey("category_fk", categoryGUID, categories)(_.guid)
    def * = (categoryGUID, publicationGUID)
  }

  class PublicationFiles(tag: Tag) extends Table[PublicationFile](tag, "publication_files") {
    def guid = column[UUID]("guid", O.PrimaryKey)
    def name = column[String]("name", O.Length(256))
    def contentType = column[String]("content_type", O.Length(64))
    def size = column[Long]("size")
    def url = column[String]("url")
    def urlIndex = index("url_idx", url, true)
    def addedAt = column[DateTime]("added_at")
    def available = column[Boolean]("available", O.Default(true))
    def publicationGUID = column[UUID]("publication_guid")
    def publication = foreignKey("publication_fk", publicationGUID, publications)(_.guid)
    def * = (guid, publicationGUID, name, contentType, size, url, addedAt, available) <> (PublicationFile.tupled, PublicationFile.unapply _)
  }

  // this could be a view but it sucks with slick.
  // we will update it manually
  class PublicationSearches(tag: Tag) extends Table[(UUID, TsVector, String)](tag, "publication_searches") {
    def publicationGUID = column[UUID]("publication_guid")
    def publicationIndex = index("publication_search_idx", publicationGUID, true)
    def publication = foreignKey("publication_search_fk", publicationGUID, publications)(_.guid, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade)
    def vector = column[TsVector]("vector")
    def languageConfig = column[String]("language")
    def * = (publicationGUID, vector, languageConfig)
  }
  lazy val authorsComplete = TableQuery[Authors]
  lazy val authors = authorsComplete.map(_.forSelect)
  val publications = TableQuery[Publications]
  val categories = TableQuery[Categories]
  val publicationAuthors = TableQuery[PublicationAuthors]
  val publicationCategories = TableQuery[PublicationCategories]
  val publicationStatuses = TableQuery[PublicationStatuses]
  val publicationFiles = TableQuery[PublicationFiles]
  val publicationSearches = TableQuery[PublicationSearches]
  override def tables = Seq(authorsComplete, publications, categories, publicationAuthors, publicationCategories, publicationStatuses, publicationFiles, publicationSearches)
  override def createAdditionalActions = Seq(
    sqlu"""
    create index "publication_search_vector_idx" on "publication_searches" using gin(vector);
    """,
    // index author and make a trigger to update it.
    sqlu"""
      create index "author_tv_idx" on "authors" using gin(vector);
      """,
    sqlu"""
              create trigger authorTVUpdate before insert or update
      on authors for each row execute procedure
      tsvector_update_trigger(vector, 'pg_catalog.portuguese', name);
"""
  )
}

