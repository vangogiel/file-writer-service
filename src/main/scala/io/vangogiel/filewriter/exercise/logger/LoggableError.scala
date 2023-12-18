package io.vangogiel.filewriter.exercise.logger

import julienrf.json.derived
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils
import play.api.libs.json.{ OWrites, __ }

sealed trait LoggableError {
  def error: String
}

object LoggableError {
  case class Error(error: String) extends LoggableError
  implicit lazy val writes: OWrites[LoggableError] =
    derived.flat
      .owrites[LoggableError](
        (__ \ "errorName")
          .write[String]
          .contramap[String](StringUtils.uncapitalize)
      )
}
