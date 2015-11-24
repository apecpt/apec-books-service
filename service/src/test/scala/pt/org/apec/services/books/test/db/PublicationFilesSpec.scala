package pt.org.apec.services.books.test.db

import org.scalatest._
import pt.org.apec.services.books.common._
import spray.http.StatusCodes

class PublicationFilesSpec extends BaseRouteSpec with BasicData with Matchers {
  "Publication files" should "Have an emptypublication files list when no files were created" in {
    val memorial = createMemorial
    Get(s"/publications/${memorial.slug}/files/") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Seq[PublicationFile]] should be('empty)
    }
  }
  it should "Allow creation of new file information" in {
    val memorial = createMemorial
    val request = NewPublicationFileRequest("memorial.txt", "text/plain", 1000, "http://example.com/memorial.txt")
    Post(s"/publications/${memorial.slug}/files/", request) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.Created
      responseAs[PublicationFile].publicationGUID shouldBe memorial.guid
    }
    Get(s"/publications/${memorial.slug}/files") ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Seq[PublicationFile]] should have size 1
    }
  }
}