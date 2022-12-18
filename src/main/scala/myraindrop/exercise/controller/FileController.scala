package myraindrop.exercise.controller

import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import cats.effect.{ ContextShift, IO }
import myraindrop.exercise.actor.RequestsRateLimitingActor
import myraindrop.exercise.actor.RequestsRateLimitingActor.{ Allowed, LimitReached, RequestResource }
import myraindrop.exercise.api.{ CreateFileRequest, RequestBodyParser }
import myraindrop.exercise.logger.{ CreateFileRequestReceived, TypedLogger }
import play.api.mvc.{ AbstractController, Action, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

class FileController(
    requestsRateActor: ActorRef[RequestsRateLimitingActor.Command],
    parser: RequestBodyParser,
    cc: ControllerComponents,
    log: TypedLogger
)(implicit val typedSystem: ActorSystem[Nothing], timeout: Timeout)
    extends AbstractController(cc) {
  implicit val ec: ExecutionContext = typedSystem.executionContext
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  def postCreateFile(): Action[CreateFileRequest] = Action.async(parser.parseCreateFileRequest()) { implicit request =>
    log.info(CreateFileRequestReceived(request.body.requestId))
    for {
      response <- IO
        .fromFuture(
          IO.delay(
            requestsRateActor.ask[RequestsRateLimitingActor.Response](RequestResource(request.body.requestId, _))
          )
        )
        .unsafeRunSync() match {
        case Allowed()      => Future.successful(Ok)
        case LimitReached() => Future.successful(TooManyRequests)
      }
    } yield response
  }
}
