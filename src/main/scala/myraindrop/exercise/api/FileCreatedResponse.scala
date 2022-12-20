package myraindrop.exercise.api

import play.api.libs.json.{ Json, OWrites }

case class FileCreatedResponse(requestId: String, created: Boolean, fileContent: Option[String])

object FileCreatedResponse {
  implicit val writes: OWrites[FileCreatedResponse] = Json.writes[FileCreatedResponse]
}
