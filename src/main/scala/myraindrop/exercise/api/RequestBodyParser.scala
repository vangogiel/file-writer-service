package myraindrop.exercise.api

import cats.syntax.either._
import myraindrop.exercise.api.Errors.{ BodyDoesNotMatchSchema, BodyIsEmpty, BodyIsNotJson, GeneralError }
import play.api.libs.json.{ JsValue, Json, Reads }
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class RequestBodyParser(parse: PlayBodyParsers) extends Results {
  def parseCreateFileRequest(): BodyParser[CreateFileRequest] = parseRequest[CreateFileRequest]()

  def parseRequest[A]()(implicit reads: Reads[A]): BodyParser[A] = {
    buildBodyParser[A](
      parse,
      either => {
        val result: Either[GeneralError, A] = either match {
          case Right(json) =>
            json
              .validate[A]
              .asEither
              .leftMap(BodyDoesNotMatchSchema.fromJsErrors)
          case Left(false) => Left(BodyIsEmpty)
          case Left(true)  => Left(BodyIsNotJson)
        }

        result.leftMap(errors => BadRequest(Json.toJson[ErrorResponse](ErrorResponse(error = Seq(errors)))))
      }
    )
  }

  private def buildBodyParser[A](
      parse: PlayBodyParsers,
      f: Either[Boolean, JsValue] => Either[Result, A]
  ): BodyParser[A] =
    BodyParser(header =>
      parse.tolerantJson
        .apply(header)
        .map(either => f(either.leftMap(_ => header.hasBody)))
    )
}
