package io.vangogiel.filewriter.exercise.api

import play.api.libs.json.{ Json, Reads, Writes }

case class CreateFileRequest(requestId: String)

object CreateFileRequest {
  implicit val reads: Reads[CreateFileRequest] = Json.reads[CreateFileRequest]
  implicit val writes: Writes[CreateFileRequest] = Json.writes[CreateFileRequest]
}
