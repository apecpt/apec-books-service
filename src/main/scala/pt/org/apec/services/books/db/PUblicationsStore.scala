package pt.org.apec.services.books.db

import slick.driver.PostgresDriver.api._
import scala.concurrent.ExecutionContext

/**
 * @author ragb
 */
class PublicationsStore(db : Database)(implicit executorContext : ExecutionContext) {
  def createSchema = db.run(Tables.schema.create)
  
}