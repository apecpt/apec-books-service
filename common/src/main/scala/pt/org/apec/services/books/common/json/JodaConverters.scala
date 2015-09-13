package pt.org.apec.services.books.common.json

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ Period, LocalDate, DateTime, Duration }
import play.api.data.validation.ValidationError
import play.api.libs.json._
import scala.math.BigDecimal.long2bigDecimal

/**
 * @author dbalduini
 */
object JodaConverters {

  val fmt = ISODateTimeFormat.dateTime

  /*
  * DateTime Json formats
  */
  implicit val isoDateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
    override def writes(o: DateTime): JsValue = JsString(fmt print o)
  }
  implicit val isoDateTimeReads: Reads[DateTime] = new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = json match {
      case JsString(value) => JsSuccess(fmt parseDateTime value)
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.datetime"))))
    }
  }

  /*
  * LocalDate Json formats
  */
  implicit val localDateWrites: Writes[LocalDate] = new Writes[LocalDate] {
    override def writes(o: LocalDate): JsValue = JsString(fmt print o)
  }
  implicit val localDateReads: Reads[LocalDate] = new Reads[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] = json match {
      case JsString(value) => JsSuccess(fmt parseLocalDate value)
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.datetime"))))
    }
  }

  /*
  * Duration Json formats
   */
  implicit val durationWrites: Writes[Duration] = new Writes[Duration] {
    def writes(o: Duration): JsValue = JsNumber(o.getMillis)
  }
  implicit val durationReads: Reads[Duration] = new Reads[Duration] {
    def reads(json: JsValue): JsResult[Duration] = json match {
      case JsNumber(value) => JsSuccess(new Duration(value.toLong))
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("validate.error.expected.period"))))
    }
  }

  /*
  * Period Json formats
  */
  implicit val periodWrites: Writes[Period] = new Writes[Period] {
    def writes(o: Period): JsValue = Json.toJson(o.toStandardDuration)
  }
  implicit val periodReads: Reads[Period] = new Reads[Period] {
    def reads(json: JsValue): JsResult[Period] = durationReads.reads(json).map(_.toPeriod)
  }

}
