package pt.org.apec.services.books.api

import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import scala.concurrent.duration._
import slick.driver.PostgresDriver.api._
import pt.org.apec.services.books.db.Tables



/**
 * @author ragb
 */
object Boot extends App {
  val conf = ConfigFactory.load()
  val httpInterface = conf.getString("http.interface")
  val httpPort = conf.getInt("http.port")
  implicit val system = ActorSystem("apec-books-service")
  import system.dispatcher
  
  Tables.schema.createStatements foreach println
  //val db = Database.forConfig("db.default", conf)
  val booksServiceActor = system.actorOf(Props[BooksServiceActor])
  implicit val timeout = Timeout(5 seconds)
  IO(Http) ? Http.Bind(booksServiceActor, interface=httpInterface, port=httpPort)
}