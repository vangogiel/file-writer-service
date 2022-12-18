package myraindrop.exercise

import akka.actor.testkit.typed.scaladsl.{ ActorTestKit, ScalaTestWithActorTestKit }
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import myraindrop.exercise.actor.RequestsRateLimitingActor
import myraindrop.exercise.api.{ CreateFileRequest, RequestBodyParser }
import myraindrop.exercise.controller.FileController
import myraindrop.exercise.logger.{ CreateFileRequestReceived, TestLogger }
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.{ OK, TOO_MANY_REQUESTS }
import play.api.test.FakeRequest
import play.api.test.Helpers.{ status, stubBodyParser, stubControllerComponents }

class FileControllerSpec extends ScalaTestWithActorTestKit with MockFactory with AnyWordSpecLike {
  val actorSystem: ActorTestKit = ActorTestKit()
  override implicit val system: ActorSystem[Nothing] = actorSystem.system

  val mockBodyParser: RequestBodyParser = mock[RequestBodyParser]
  val mockLogger: TestLogger = new TestLogger
  val mockRequestIdResponseAllowed = "mockRequestId1"
  val mockRequestIdResponseLimitReached = "mockRequestId2"

  "FileController" should {
    "respond with 200 upon valid create file attempt" in {
      prepareBodyParserForRequest1

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      status(result) mustBe OK
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
    }

    "respond with 429 upon valid create file attempt with requests limit reached" in {
      prepareBodyParserForRequest2

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      status(result) mustBe TOO_MANY_REQUESTS
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
    }
  }

  private def buildController = {
    new FileController(
      actorSystem.spawn(rateLimitingBehaviour()),
      mockBodyParser,
      stubControllerComponents(),
      mockLogger
    )
  }

  private def prepareBodyParserForRequest1 = prepareBodyParserForRequest(mockRequestIdResponseAllowed)

  private def prepareBodyParserForRequest2 = prepareBodyParserForRequest(mockRequestIdResponseLimitReached)

  private def prepareBodyParserForRequest(requestId: String) =
    (() => mockBodyParser.parseCreateFileRequest())
      .expects()
      .returning(stubBodyParser[CreateFileRequest](CreateFileRequest(requestId)))

  private def rateLimitingBehaviour() = Behaviors.receiveMessage[RequestsRateLimitingActor.Command] {
    case RequestsRateLimitingActor.RequestResource(r, sender) =>
      if (r == mockRequestIdResponseAllowed) {
        sender ! RequestsRateLimitingActor.Allowed()
        Behaviors.same
      } else {
        sender ! RequestsRateLimitingActor.LimitReached()
        Behaviors.same
      }
    case _ => Behaviors.empty
  }
}
