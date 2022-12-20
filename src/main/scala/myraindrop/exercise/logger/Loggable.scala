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

case class FileFoundResponse(requestId: String) extends Loggable {
  override def message: String = "Responding with a valid content of a file"
}

object FileFoundResponse {
  implicit val writes: OWrites[FileFoundResponse] =
    derived.flat
      .owrites[FileFoundResponse](
        (__ \ "messageType").write[String].contramap[String](StringUtils.uncapitalize)
      )
}

case class FileIsBeingCreatedResponse(requestId: String) extends Loggable {
  override def message: String = "Returning file is being created response"
}

object FileIsBeingCreatedResponse {
  implicit val writes: OWrites[FileIsBeingCreatedResponse] =
    derived.flat
      .owrites[FileIsBeingCreatedResponse](
        (__ \ "messageType").write[String].contramap[String](StringUtils.uncapitalize)
      )
}

case class TooManyRequestsResponse(requestId: String) extends Loggable {
  override def message: String = "Returning too many requests to the caller"
}

object TooManyRequestsResponse {
  implicit val writes: OWrites[TooManyRequestsResponse] =
    derived.flat
      .owrites[TooManyRequestsResponse](
        (__ \ "messageType").write[String].contramap[String](StringUtils.uncapitalize)
      )
}
