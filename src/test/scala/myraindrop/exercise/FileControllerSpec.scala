package myraindrop.exercise

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import myraindrop.exercise.api.{ CreateFileRequest, RequestBodyParser }
import myraindrop.exercise.controller.FileController
import myraindrop.exercise.logger.{ CreateFileRequestReceived, TestLogger }
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{ defaultAwaitTimeout, status, stubBodyParser, stubControllerComponents }

class FileControllerSpec extends PlaySpec with MockFactory {
  val actorSystem: ActorTestKit = ActorTestKit()
  implicit val system: ActorSystem[Nothing] = actorSystem.system

  val mockBodyParser: RequestBodyParser = mock[RequestBodyParser]
  val mockLogger: TestLogger = new TestLogger

  "FileController" should {
    "respond with 200 upon valid create file attempt" in {
      prepareBodyParser

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      status(result) mustBe OK
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
    }
  }

  private def buildController = {
    new FileController(
      mockBodyParser,
      stubControllerComponents(),
      mockLogger
    )
  }

  private def prepareBodyParser =
    (() => mockBodyParser.parseCreateFileRequest())
      .expects()
      .returning(stubBodyParser[CreateFileRequest](createCreateFileRequest))

  private val createCreateFileRequest = CreateFileRequest("mockRequestId")
}
