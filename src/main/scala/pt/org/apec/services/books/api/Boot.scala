package pt.org.apec.services.books.api

import akka.io.IO
import spray.can.Http
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import slick.driver.PostgresDriver.api._
import pt.org.apec.services.books.db.Tables
import pt.org.apec.services.books.db.PublicationsStore

/**
 * @author ragb
 */
object Boot {
  val conf = ConfigFactory.load()
  implicit val system = ActorSystem("apec-books-service")
  import system.dispatcher

  val db = Database.forConfig("db.default", conf)
  val store = new PublicationsStore(db)

  def main(args: Array[String]) {
    args.toList match {
      case "initdb" :: nil => initDB()
      case nil => run()
      case _ => {
        println("unknown arguemtns")
        sys.exit()
      }
    }
  }

  def run() {
    val httpInterface = conf.getString("http.interface")
    val httpPort = conf.getInt("http.port")

    val booksServiceActor = system.actorOf(Props(new BooksServiceActor(store)))
    implicit val timeout = Timeout(5 seconds)
    IO(Http) ? Http.Bind(booksServiceActor, interface = httpInterface, port = httpPort)
  }

  def initDB() {
    println("Initializing db schema")
    Await.result(store.createSchema, 10 seconds)
    println("done")
    sys.exit()
  }
}