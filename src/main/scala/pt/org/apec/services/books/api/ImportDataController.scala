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
      publications <- Future.sequence(request.rawPublications.map(p => importRawPublication(p).map(Some(_))).map(_.recover { case e : DuplicateFound => None}))
    } yield ImportDataResult(categories,authors, statuses, publications collect {case Some(e) => e},Seq.empty)
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
      newCategories = categorySlugs &~ categories.map(_.slug).toSet
      newAuthors = authorSlugs &~ authors.map(_.slug).toSet
       newStatuses = statusSlugs &~ statuses.map(_.slug).toSet
      categoryRequests = newCategories.toSeq map(NewCategoryRequest(_))
      authorRequests = newAuthors.toSeq map(a => NewAuthorRequest(a, a))
       statusRequests = newStatuses.toSeq map(NewPublicationStatusRequest(_, 0))
      // could batch this at least...dumb me.
      cs <- Future.sequence(categoryRequests.map(publicationsStore.createCategory(_)))
      as <- Future.sequence(authorRequests.map(publicationsStore.createAuthor(_)))
      ss <- Future.sequence(statusRequests.map(publicationsStore.createPublicationStatus(_)))
    } yield(cs, as, ss)
  }
  
  private def importRawPublication(publication : RawPublication) = for {
    maybeCategory <- publicationsStore.getCategoryBySlug(publication.category)
    category <- maybeCategory.map(Future.successful).getOrElse(Future.failed(ImportDataException("Category not found: "+ publication.category)))
    maybeAuthor <- publicationsStore.getAuthorBySlug(publication.author)
    author <- maybeAuthor.map(Future.successful).getOrElse(Future.failed(ImportDataException("author not found:" + publication.author)))
    maybeStatus <- publication.status map (publicationsStore.getPublicationStatusBySlug) getOrElse(Future.successful(None))
    result <- publicationsStore.createPublication(NewPublicationRequest(publication.title, publication.title, Seq(author.guid), Seq(category.guid), None, None, maybeStatus.map(_.guid)))
  } yield(result)
}

object ImportDataController {
  case class RawPublication(title : String, author : String, category : String, status : Option[String], format : String, notes : Option[String])
  
  case class ImportRawPublicationsRequest(rawPublications : Seq[RawPublication])
  
  case class ImportDataResult(addedCategories : Seq[Category], addedAuthors : Seq[Author], addedPublicationStatuses : Seq[PublicationStatus], addedPublications : Seq[PublicationInfo], errors : Seq[String])
  
  
  case class ImportDataException(msg : String) extends Exception(msg)
}