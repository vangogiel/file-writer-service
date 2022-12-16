package myraindrop.exercise.logger

import julienrf.json.derived
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils
import play.api.libs.json.{ OWrites, __ }

sealed trait Loggable {
  def message: String
}

case class CreateFileRequestReceived(requestId: String) extends Loggable {
  override def message: String = "Received a valid request to create a file"
}

object CreateFileRequestReceived {
  implicit val writes: OWrites[CreateFileRequestReceived] =
    derived.flat
      .owrites[CreateFileRequestReceived](
        (__ \ "messageType").write[String].contramap[String](StringUtils.uncapitalize)
      )
}
