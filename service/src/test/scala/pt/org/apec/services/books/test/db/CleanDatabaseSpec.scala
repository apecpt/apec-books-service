package pt.org.apec.services.books.test.db

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import pt.org.apec.services.books.db._
import slick.driver.PostgresDriver
import CustomPostgresDriver.api.Database
import org.scalatest.concurrent.ScalaFutures
import godiva.slick._

/**
 * @author ragb
 */
trait CleanDatabaseSpec extends FlatSpec with BeforeAndAfter {
  val database = Database.forConfig("db.test")
  val publicationsStore = new PublicationsStore with DriverComponent[CustomPostgresDriver] with DatabaseComponent[CustomPostgresDriver] with DefaultExecutionContext {
    val driver = CustomPostgresDriver
    val database = CleanDatabaseSpec.this.database
    val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  }
  before {
    Await.result(publicationsStore.createTables, 10 seconds)
  }

  after {
    Await.result(publicationsStore.dropTables, 10 seconds)
  }
}