package pt.org.apec.services.books.test.db

import spray.testkit.ScalatestRouteTest
import pt.org.apec.services.books.api.BooksService

trait BaseRouteSpec extends CleanDatabaseSpec with ScalatestRouteTest with BooksService {
  override def actorRefFactory = system
  override val executionContext = system.dispatcher

}