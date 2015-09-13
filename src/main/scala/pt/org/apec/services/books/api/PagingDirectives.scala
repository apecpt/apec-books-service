package pt.org.apec.services.books.api

import spray.routing._
import spray.routing.Directives._
import shapeless._

trait PagingDirectives {
  case class Paginate(pageNumber: Int, pageSize: Int)

  sealed trait SortOrder
  case object Asc extends SortOrder
  case object Desc extends SortOrder

  case class Sort(attribute: String, order: SortOrder = Asc)
  case class SortBy(sorts: Seq[Sort])

  val defaultPageSize = 20

  def paginated: Directive1[Option[Paginate]] = readPageParameters.hmap {
    case Some(pageNumber) :: pageSize :: HNil => Some(Paginate(pageNumber, pageSize.getOrElse(defaultPageSize)))
    case _ => None
  }

  private val readPageParameters: Directive[Option[Int] :: Option[Int] :: HNil] = parameters("pageNumber".as[Int].?, "pageSize".as[Int].?)

  def sortedBy: Directive1[SortBy] = readSortParameter.hmap {
    case Some(sortParam) :: HNil => {
      val params: Seq[Sort] = sortParam.split(",") map { p =>
        if (p.startsWith("-")) Sort(p.substring(1), Desc) else Sort(p, Asc)
      }
      SortBy(params)
    }
    case _ => SortBy(Seq.empty)
  }

  private val readSortParameter: Directive[Option[String] :: HNil] = parameters("sortby".as[String].?)
}

object PagingDirectives extends PagingDirectives