package io.vangogiel.filewriter.exercise.controller

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.util.Timeout
import cats.effect.{ ContextShift, IO }
import io.vangogiel.filewriter.exercise.api.Errors.InternalError
import io.vangogiel.filewriter.exercise.api.{ CreateFileRequest, ErrorResponse, FileCreatedResponse, RequestBodyParser }
import io.vangogiel.filewriter.exercise.logger.LoggableError.Error
import io.vangogiel.filewriter.exercise.logger._
import io.vangogiel.filewriter.exercise.service.FilesServiceActor
import io.vangogiel.filewriter.exercise.service.FilesServiceActor.{ FileFound, FileIsBeingCreated, GetOrCreateFile }
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

class FileController(
    requestsRateActor: ActorRef[RequestsRateLimitingActor.Command],
    filesServiceActor: ActorRef[FilesServiceActor.Command],
    parser: RequestBodyParser,
    cc: ControllerComponents,
    log: TypedLogger
)(implicit val typedSystem: ActorSystem[Nothing], timeout: Timeout)
    extends AbstractController(cc) {
  implicit val ec: ExecutionContext = typedSystem.executionContext
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  def postCreateFile(): Action[CreateFileRequest] = Action.async(parser.parseCreateFileRequest()) { implicit request =>
    val requestId = request.body.requestId
    log.info(CreateFileRequestReceived(requestId))
    val result = for {
      _ <- wrappedRatesActorAsk(requestId)
      result <- wrappedFilesServiceAsk(requestId)
    } yield result

    result match {
      case Right(FileFound(contents)) =>
        log.info(FileFoundResponse(requestId))
        Future.successful(Ok(Json.toJson(FileCreatedResponse(requestId, created = true, Option(contents)))))
      case Left(FileFound(contents)) =>
        log.info(FileFoundResponse(requestId))
        Future.successful(Ok(Json.toJson(FileCreatedResponse(requestId, created = true, Option(contents)))))
      case Left(FileIsBeingCreated()) =>
        log.info(FileIsBeingCreatedResponse(requestId))
        Future.successful(Ok(Json.toJson(FileCreatedResponse(requestId, created = false, Option.empty))))
      case Left(LimitReached()) =>
        log.info(TooManyRequestsResponse(requestId))
        Future.successful(TooManyRequests)
      case Left(_) =>
        logError(Error("Unexpected error occurred"))
        Future.successful(InternalServerError(Json.toJson(ErrorResponse(error = Seq(InternalError)))))
    }
  }

  private def wrappedRatesActorAsk(requestId: String) = {
    IO
      .fromFuture(
        IO(requestsRateActor.ask[RequestsRateLimitingActor.Response](RequestResource(requestId, _)))
      )
      .unsafeRunSync() match {
      case Allowed()      => Right(Allowed())
      case LimitReached() => Left(LimitReached())
    }
  }

  private def wrappedFilesServiceAsk(requestId: String) = {
    IO
      .fromFuture(
        IO(filesServiceActor.ask[FilesServiceActor.Response](GetOrCreateFile(requestId, _)))
      )
      .unsafeRunSync() match {
      case FileFound(fileContents) => Right(FileFound(fileContents))
      case FileIsBeingCreated()    => Left(FileIsBeingCreated())
    }
  }

  private def logError(error: LoggableError): Unit = log.error(error)
}
