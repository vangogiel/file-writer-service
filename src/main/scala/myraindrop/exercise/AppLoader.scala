package myraindrop.exercise

import com.softwaremill.macwire.wire
import myraindrop.exercise.api.RequestBodyParser
import myraindrop.exercise.controller.{ FileController, HealthController }
import myraindrop.exercise.logger.{ AppLogger, TypedLogger }
import play.api.routing.Router
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext, NoHttpFiltersComponents }
import router.Routes

case class AppComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    with NoHttpFiltersComponents
    with controllers.AssetsComponents {
  val logger: TypedLogger = new AppLogger
  val prefix: String = "/"
  lazy val requestBodyParser: RequestBodyParser = wire[RequestBodyParser]
  lazy val healthController: HealthController = wire[HealthController]
  lazy val fileController: FileController = wire[FileController]
  override def router: Router = wire[Routes]
}

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = AppComponents(context).application
}
