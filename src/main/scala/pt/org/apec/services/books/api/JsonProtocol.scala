package pt.org.apec.services.books.api

import play.api.libs.json._
import spray.httpx.PlayJsonSupport
import pt.org.apec.services.books.common._
import pt.org.apec.services.books.common.json._
import pt.org.apec.services.books.db._


trait JsonProtocol extends PlayJsonSupport with JsonFormaters {
    implicit val DuplicateFoundWrites = Json.writes[DatabaseException]

}

