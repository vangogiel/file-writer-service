package io.vangogiel.filewriter.exercise.api

import play.api.libs.json.{ JsPath, JsString, Writes }

case class JsonPath(jsPath: JsPath)

object JsonPath {
  implicit val writes: Writes[JsonPath] = jsonPath => JsString(jsonPath.jsPath.toJsonString.replaceFirst("obj", "body"))
}
