package io.vangogiel.filewriter.exercise

import akka.Done
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AsyncWordSpecLike
import play.api.ApplicationLoader.Context
import play.api.libs.concurrent.ActorSystemProvider.ApplicationShutdownReason
import play.api.{ ApplicationLoader, Environment, Mode }

import java.io.File
import scala.concurrent.ExecutionContext

class AppLoaderSpec extends AsyncWordSpecLike with MockFactory {
  implicit val ec: ExecutionContext = ExecutionContext.global
  val context: Context = Context.create(
    new Environment(
      new File("."),
      ApplicationLoader.getClass.getClassLoader,
      Mode.Test
    )
  )

  "The Application" should {
    "start" in {
      val app = (new AppLoader).load(context)
      val future = app.coordinatedShutdown.run(ApplicationShutdownReason)

      future.map { p =>
        assert(p == Done)
      }
    }
  }
}
