package pt.org.apec.services.books.db

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate

/**
 * @author ragb
 */
class PublicationsStore(db : Database)(implicit executorContext : ExecutionContext) {
  def createSchema = db.run(Tables.schema.create)
  def dropSchema = db.run(Tables.schema.drop)
  

}

case class PublicationInfo(guid : UUID, authors : Seq[Author], categories : Seq[Category], title : String, publicationYear : Option[Int], createdAt : LocalDateTime, updatedAt : Option[LocalDateTime], notes : Option[String])