package io.vangogiel.filewriter.exercise.logger

import play.api.libs.json.{ JsString, Json, OWrites }

case class JsonThrowable(throwable: Throwable)

object JsonThrowable {
  implicit val writes: OWrites[JsonThrowable] = event => {
    val json = Json.obj("name" -> event.throwable.getClass.getName)
    Option(event.throwable.getMessage)
      .filter(_.trim.nonEmpty)
      .fold(json)(message => json + (("message", JsString(message))))
  }
}
