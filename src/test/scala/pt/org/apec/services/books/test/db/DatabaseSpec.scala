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
import pt.org.apec.services.books.test.PostgresqlDockerContainer
import slick.driver.PostgresDriver.api.Database
import org.scalatest.concurrent.ScalaFutures



/**
 * @author ragb
 */
trait DatabaseSpec extends FlatSpec with PostgresqlDockerContainer with BeforeAndAfter with ScalaFutures {
  val config = ConfigFactory.load()
  .withValue("db.default", ConfigValueFactory.fromMap(
      Map("user" -> databaseUser,
          "password" -> databasePassword,
          "driver" -> "org.postgresql.Driver",
          "url" -> s"jdbc:postgresql://${databaseHostName}/${databaseName}?user=${databaseUser}")))
  val database = Database.forConfig("db.default", config)
      val publicationsStore = new PublicationsStore(database)

  before {
    Await.result(publicationsStore.createSchema, 10 seconds)
  }
  
  after {
    Await.result(publicationsStore.dropSchema, 10 seconds)
  }
}