package pt.org.apec.services.books.api

import spray.routing._
import spray.http._
import akka.actor.Actor


class BooksServiceActor extends Actor with BooksService {
  def actorRefFactory = context
  
  def receive = runRoute(routes)
}

/**
 * @author ragb
 */
trait BooksService extends HttpService {
  def routes = (path("") & get) {
    complete {
    <html>hello</html>
    }
  }
}