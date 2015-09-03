package pt.org.apec.services.books.api

import java.util.UUID

import scala.concurrent.{Future, ExecutionContext}
import pt.org.apec.services.books.common._
import pt.org.apec.services.books.db._

/**
 * @author ragb
 */
class ImportDataController(val publicationsStore : PublicationsStore)(implicit executionContext : ExecutionContext) {
  import ImportDataController._
  def importRawPublications(request : ImportRawPublicationsRequest) : Future[ImportDataResult] = {
    for {
      (categories, authors, statuses) <- importMetadata(request)
    } yield ImportDataResult(categories,authors, statuses, Seq.empty,Seq.empty)
  }

  private def importMetadata(request : ImportRawPublicationsRequest) = {
        // make this kinda transactional at least.
    val categorySlugs = request.rawPublications.map(_.category).toSet
    val authorSlugs = request.rawPublications.map(_.author).toSet
    val statusSlugs = request.rawPublications.map(_.status).collect { case Some(e) => e} toSet
    val categoriesF = publicationsStore.getCategories
    val authorsF = publicationsStore.getAuthors
    val statusesF = publicationsStore.getPublicationStatuses
    for {
      ((categories, authors), statuses) <- categoriesF zip authorsF zip statusesF
      val newCategories = categorySlugs &~ categories.map(_.slug).toSet
      val newAuthors = authorSlugs &~ authors.map(_.slug).toSet
      val newStatuses = statusSlugs &~ statuses.map(_.slug).toSet
      val categoryRequests = newCategories.toSeq map(NewCategoryRequest(_))
      val authorRequests = newAuthors.toSeq map(a => NewAuthorRequest(a, a))
      val statusRequests = newStatuses.toSeq map(NewPublicationStatusRequest(_, 0))
      // could batch this at least...dumb me.
      cs <- Future.sequence(categoryRequests.map(publicationsStore.createCategory(_)))
      as <- Future.sequence(authorRequests.map(publicationsStore.createAuthor(_)))
      ss <- Future.sequence(statusRequests.map(publicationsStore.createPublicationStatus(_)))
    } yield(cs, as, ss)
  }
}

object ImportDataController {
  case class RawPublication(title : String, author : String, category : String, status : Option[String], format : String, notes : Option[String])
  
  case class ImportRawPublicationsRequest(rawPublications : Seq[RawPublication])
  
  case class ImportDataResult(addedCategories : Seq[Category], addedAuthors : Seq[Author], addedPublicationStatuses : Seq[PublicationStatus], addedPublications : Seq[PublicationInfo], errors : Seq[String])
  
  
}