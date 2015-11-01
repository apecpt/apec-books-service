package pt.org.apec.services.books.api

import spray.routing._
import pt.org.apec.services.books.common.PublicationSorting

trait PublicationOrderDirectives extends Directives {
  import PublicationSorting._

  def publicationsSorted: Directive1[PublicationOrder] = readParameter("sortBy") map (_.getOrElse(PublicationOrder(CreatedAt, Desc)))
  private def readParameter(name: String) = parameter(name.?) map {
    case Some("title") => Some(PublicationOrder(Title, Asc))
    case Some("createdAt") => Some(PublicationOrder(CreatedAt, Desc))
    case _ => None
  }
}