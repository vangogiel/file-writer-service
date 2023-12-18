package io.vangogiel.filewriter.exercise.api

import io.vangogiel.filewriter.exercise.api.PlayJsonValidationErrors.{ ExpectedString, PathMissing }
import play.api.libs.json.{ JsPath, JsonValidationError, Writes, __ }

object Validation {
  sealed trait FieldValidationError {
    def code: String
    def message: String
    def field: JsonPath
  }

  case class FieldIsMissing(field: JsonPath) extends FieldValidationError {
    val code = "value_error.missing"
    val message = "Field is mandatory"
  }

  case class FieldMustBeString(field: JsonPath) extends FieldValidationError {
    val code = "value_error.expected.string"
    val message = "Field must be a string"
  }

  case class FieldHasInvalidValue(field: JsonPath) extends FieldValidationError {
    val code = "value_error.invalid_value"
    val message = "Field has invalid value"
  }

  object FieldValidationError {
    implicit val writes: Writes[FieldValidationError] = error =>
      (__ \ "code")
        .write[String]
        .writes(error.code)
        .++((__ \ "message").write[String].writes(error.message))
        .++((__ \ "field").write[JsonPath].writes(error.field))

    def convertFromJsonValidationError(jsPath: JsPath, error: JsonValidationError): FieldValidationError = {
      val path = JsonPath(jsPath)
      error.message match {
        case PathMissing    => FieldIsMissing(path)
        case ExpectedString => FieldMustBeString(path)
        case _              => FieldHasInvalidValue(path)
      }
    }
  }
}
