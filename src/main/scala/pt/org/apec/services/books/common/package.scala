package pt.org.apec.services.books

/**
 * @author ragb
 */
package object common {
  import java.util.UUID
  import org.joda.time.DateTime

  case class Author(guid: UUID, name: String, slug: String)
  case class Category(guid: UUID, slug: String)
  case class Publication(guid: UUID, title: String, slug: String, publicationYear: Option[Int], createdAt: DateTime, updatedAt: Option[DateTime], notes: Option[String] = None)
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


}