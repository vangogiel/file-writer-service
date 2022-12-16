package myraindrop.exercise.logger

import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers.appendRaw
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{ JsObject, JsPath, Json, OWrites }
import play.api.{ Logger, MarkerContext }

import java.util.UUID
import scala.reflect.runtime.universe._

sealed trait LogItem[T] {
  val logId: UUID = UUID.randomUUID()
  val detail: T
}

final case class CorrelationId(value: String) extends AnyVal

final case class CorrelatedLogItem[T <: Loggable](
    detail: T
) extends LogItem[T]

final case class CorrelatedErrorLogItem[T <: LoggableError](
    detail: T
) extends LogItem[T]

object CorrelatedLogItem {
  implicit def correlatedLogItemWrites[T <: Loggable: OWrites: TypeTag]: OWrites[CorrelatedLogItem[T]] =
    (
      (JsPath \ "logId").write[UUID] and
        (JsPath \ "eventName").write[String] and
        (JsPath \ "detail").write[T]
    ) { item: CorrelatedLogItem[T] =>
      (item.logId, getEventName[T], item.detail)
    }

  private def getEventName[T: TypeTag]: String =
    StringUtils.uncapitalize(typeOf[T].typeSymbol.name.toString)
}

object CorrelatedErrorLogItem {
  implicit def correlatedLogErrorItemWrites[T <: LoggableError: OWrites: TypeTag]: OWrites[CorrelatedErrorLogItem[T]] =
    (
      (JsPath \ "logId").write[UUID] and
        (JsPath \ "eventName").write[String] and
        (JsPath \ "detail").write[T]
    ) { item: CorrelatedErrorLogItem[T] =>
      (item.logId, getEventName[T], item.detail)
    }

  private def getEventName[T: TypeTag]: String =
    StringUtils.uncapitalize(typeOf[T].typeSymbol.name.toString)
}

trait TypedLogger {
  def info[T <: Loggable: OWrites: TypeTag](event: T)(implicit mc: MarkerContext): Unit
  def error[T <: LoggableError: OWrites: TypeTag](event: T)(implicit mc: MarkerContext): Unit
}

class AppLogger extends TypedLogger {
  private val logger = Logger("application")

  override def info[T <: Loggable: OWrites: TypeTag](event: T)(implicit mc: MarkerContext): Unit = {
    logger.info(event.message)(
      convertToMarkerContext(CorrelatedLogItem(event))
    )
  }

  override def error[T <: LoggableError: OWrites: TypeTag](event: T)(implicit mc: MarkerContext): Unit = {
    logger.error(event.error)(
      convertToMarkerContext(CorrelatedErrorLogItem(event))
    )
  }

  private def convertToMarkerContext[T <: LogItem[_]: OWrites](item: T): MarkerContext = {
    val jsObject = Json.toJsObject(item)
    MarkerContext(convertJsObjectToMarker(jsObject))
  }

  private def convertJsObjectToMarker(jsObject: JsObject): LogstashMarker = {
    jsObject.value
      .map { case (string, value) => appendRaw(string, value.toString) }
      .reduce((a, b) => a.and[LogstashMarker](b))
  }
}
