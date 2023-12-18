package io.vangogiel.filewriter.exercise.api

import io.vangogiel.filewriter.exercise.api.Errors.GeneralError
import play.api.libs.json.{ Json, OWrites }

import scala.collection.Seq

final case class ErrorResponse(status: String = "error", error: Seq[GeneralError])

object ErrorResponse {
  implicit val writes: OWrites[ErrorResponse] = Json.writes[ErrorResponse]
}
