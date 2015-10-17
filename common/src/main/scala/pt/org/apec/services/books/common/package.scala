package pt.org.apec.services.books

/**
 * @author ragb
 */
package object common {
  import java.util.UUID
  import org.joda.time.DateTime

  case class Author(guid: UUID, name: String, slug: String)
  case class Category(guid: UUID, name: String, slug: String)
  case class PublicationStatus(guid: UUID, name: String, slug: String, score: Int)
  case class Publication(guid: UUID, title: String, slug: String, publicationYear: Option[Int], createdAt: DateTime, updatedAt: Option[DateTime], notes: Option[String] = None, publicationStatusGUID: Option[UUID] = None)
  case class NewCategoryRequest(name: String, slug: String) {
    require(slug.nonEmpty, "Slug must not be empty")
    require(name.nonEmpty, "Name must not be empty")
  }

  case class NewAuthorRequest(name: String, slug: String) {
    require(slug.nonEmpty, "Slug must not be empty")
    require(name.nonEmpty, "Name must not be empty")
  }

  case class NewPublicationStatusRequest(name: String, slug: String, score: Int) {
    require(slug.nonEmpty, "Slug must not be empty")
    require(score >= 0, "Score must be greater than zero")
    require(name.nonEmpty)
  }

  case class PublicationInfo(guid: UUID, authors: Seq[Author], categories: Seq[Category], title: String, slug: String, publicationYear: Option[Int], createdAt: DateTime, updatedAt: Option[DateTime], notes: Option[String], publicationStatus: Option[PublicationStatus])
  case class NewPublicationRequest(title: String, slug: String, authors: Seq[UUID], categories: Seq[UUID], publicationYear: Option[Int], notes: Option[String] = None, publicationStatusGUID: Option[UUID] = None) {
    require(authors.nonEmpty, "Authors must not be empty")
    require(categories.nonEmpty, "Categories must not be empty")
    require(slug.nonEmpty, "Slug must not be empty")
    require(title.nonEmpty, "Title must not be empty")
  }
  case class WithPublicationCount[T](element: T, publicationCount: Int)

}