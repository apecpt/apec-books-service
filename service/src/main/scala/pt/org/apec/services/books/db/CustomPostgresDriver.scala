package pt.org.apec.services.books.db

import com.github.tminglei.slickpg._

trait CustomPostgresDriver extends ExPostgresDriver with PgSearchSupport {
  override val api = new API with SearchImplicits with SearchAssistants
}

object CustomPostgresDriver extends CustomPostgresDriver
