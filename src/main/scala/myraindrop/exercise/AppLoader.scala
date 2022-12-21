package myraindrop.exercise

import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.util.Timeout
import com.softwaremill.macwire.wire
import myraindrop.exercise.actor.RequestRateEvictionActor.StartScheduler
import myraindrop.exercise.actor.{ RequestRateEvictionActor, RequestsRateLimitingActor }
import myraindrop.exercise.api.RequestBodyParser
import myraindrop.exercise.controller.{ FileController, HealthController }
import myraindrop.exercise.logger.{ AppLogger, TypedLogger }
import myraindrop.exercise.service.{ FilesAsyncFileCreator, FilesResource, FilesServiceActor }
import play.api.routing.Router
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext, NoHttpFiltersComponents }
import router.Routes

import scala.collection.immutable.Map
import scala.collection.immutable.List
import scala.concurrent.duration.DurationInt

case class AppComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    with NoHttpFiltersComponents
    with controllers.AssetsComponents {
  implicit val typedSystem: akka.actor.typed.ActorSystem[Nothing] = actorSystem.toTyped
  implicit val timeout: Timeout = Timeout(5.seconds)

  val logger: TypedLogger = wire[AppLogger]
  val filesResource: FilesResource = wire[FilesResource]
  val filesAsyncFileCreator: FilesAsyncFileCreator = wire[FilesAsyncFileCreator]

  val requestsRateActor: ActorRef[RequestsRateLimitingActor.Command] =
    actorSystem.spawn(RequestsRateLimitingActor.apply(Map()), "requests-rate-actor")
  val filesServiceActor: ActorRef[FilesServiceActor.Command] =
    actorSystem.spawn(
      FilesServiceActor.apply(List(), filesResource, filesAsyncFileCreator, typedSystem)(typedSystem.executionContext),
      "file-service-actor"
    )
  val requestRateEvictionActorActorRef: ActorRef[RequestRateEvictionActor.Command] =
    ActorSystem(
      RequestRateEvictionActor.apply(requestsRateActor),
      "jwt-sessions-eviction-actor"
    )
  requestRateEvictionActorActorRef ! StartScheduler()

  val prefix: String = "/"
  lazy val requestBodyParser: RequestBodyParser = wire[RequestBodyParser]
  lazy val healthController: HealthController = wire[HealthController]
  lazy val fileController: FileController = wire[FileController]
  override def router: Router = wire[Routes]
}

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = AppComponents(context).application
}
