package pt.org.apec.services.books.test.db

import org.scalatest._

import pt.org.apec.services.books.api.ImportDataController
import ImportDataController._

import spray.http._

/**
 * @author ragb
 */
class ImportDataSpec extends BaseRouteSpec with Matchers {
  val request1 = Seq(
      RawPublication(title="t1", author="a1", category="c1", status=Some("s1"), format="txt", notes=None),
            RawPublication(title="t2", author="a2", category="c2", status=Some("s2"), format="txt", notes=None))
            
              val request2 = Seq(
      RawPublication(title="t3", author="a1", category="c1", status=Some("s1"), format="txt", notes=None),
            RawPublication(title="t4", author="a2", category="c2", status=Some("s2"), format="txt", notes=None))
            def importData(data : Seq[RawPublication]) = {
    Post("/import/rawPublications", data) ~> sealRoute(routes) ~> check {
      status shouldBe StatusCodes.OK
      responseAs[ImportDataResult]
    }
  }

            
            "Import data controller" should "Import raw publication requests" in {
              val r = importData(request1)
              r.addedAuthors should have length 2
              r.addedCategories should have length 2
              r.addedPublicationStatuses should have length 2
              r.addedPublications should have length 2
  }
  
  it should "Not add repeated metadata" in {
    importData(request1)
    val r = importData(request2)
    r.addedAuthors should have length 0
    r.addedCategories should have length 0
    r.addedPublicationStatuses should have length 0
    r.addedPublications should have length 2
  }
}