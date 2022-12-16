package myraindrop.exercise.controller

import myraindrop.exercise.api.{ CreateFileRequest, RequestBodyParser }
import myraindrop.exercise.logger.{ CreateFileRequestReceived, TypedLogger }
import play.api.mvc.{ AbstractController, Action, ControllerComponents }

import scala.concurrent.Future

class FileController(parser: RequestBodyParser, cc: ControllerComponents, log: TypedLogger)
    extends AbstractController(cc) {
  def postCreateFile(): Action[CreateFileRequest] = Action.async(parser.parseCreateFileRequest()) { implicit request =>
    log.info(CreateFileRequestReceived(request.body.requestId))
    Future.successful(Ok)
  }
}
