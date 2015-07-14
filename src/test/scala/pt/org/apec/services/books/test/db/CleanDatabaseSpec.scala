package pt.org.apec.services.books.test.db

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import pt.org.apec.services.books.db.PublicationsStore
import slick.driver.PostgresDriver.api.Database
import org.scalatest.concurrent.ScalaFutures



/**
 * @author ragb
 */
trait CleanDatabaseSpec extends FlatSpec with BeforeAndAfter {
  val database = Database.forConfig("db.test")
      val publicationsStore = new PublicationsStore(database)
  before {
    Await.result(publicationsStore.createSchema, 10 seconds)
  }
  
  after {
    Await.result(publicationsStore.dropSchema, 10 seconds)
  }
}