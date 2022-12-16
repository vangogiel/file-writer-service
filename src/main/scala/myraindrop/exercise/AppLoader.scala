package myraindrop.exercise

import com.softwaremill.macwire.wire
import myraindrop.exercise.controller.HealthController
import play.api.routing.Router
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext, NoHttpFiltersComponents }
import router.Routes

case class AppComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    with NoHttpFiltersComponents
    with controllers.AssetsComponents {
  val prefix: String = "/"
  lazy val healthController: HealthController = wire[HealthController]
  override def router: Router = wire[Routes]
}

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = AppComponents(context).application
}
