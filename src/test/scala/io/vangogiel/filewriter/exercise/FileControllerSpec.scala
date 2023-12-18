package io.vangogiel.filewriter.exercise

import akka.actor.testkit.typed.scaladsl.{ ActorTestKit, ScalaTestWithActorTestKit }
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import io.vangogiel.fileservice.exercise.actor.RequestsRateLimitingActor
import io.vangogiel.fileservice.exercise.api.{ CreateFileRequest, RequestBodyParser }
import io.vangogiel.fileservice.exercise.controller.FileController
import io.vangogiel.fileservice.exercise.logger.{
  CreateFileRequestReceived,
  FileFoundResponse,
  FileIsBeingCreatedResponse,
  TestLogger,
  TooManyRequestsResponse
}
import io.vangogiel.fileservice.exercise.service.FilesServiceActor
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.{ OK, TOO_MANY_REQUESTS }
import play.api.test.FakeRequest
import play.api.test.Helpers.{ contentAsJson, status, stubBodyParser, stubControllerComponents }

class FileControllerSpec extends ScalaTestWithActorTestKit with MockFactory with AnyWordSpecLike {
  val actorSystem: ActorTestKit = ActorTestKit()
  override implicit val system: ActorSystem[Nothing] = actorSystem.system

  val mockBodyParser: RequestBodyParser = mock[RequestBodyParser]
  val mockLogger: TestLogger = new TestLogger
  val mockRequestIdResponseAllowed = "mockRequestId1"
  val mockRequestIdResponseLimitReached = "mockRequestId2"
  val mockRequestIdResponseFileBeingCreated = "mockRequestId3"

  "FileController" should {
    "respond with 200 upon valid create file attempt" in {
      prepareBodyParserForRequestAllowed

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      status(result) mustBe OK
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
      mockLogger.containsA[FileFoundResponse](_.requestId mustBe a[String])
    }

    "respond with valid body content and file content upon valid attempt to retrieve content" in {
      prepareBodyParserForRequestAllowed

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      val content = contentAsJson(result)
      (content \ "requestId").as[String] mustBe mockRequestIdResponseAllowed
      (content \ "created").as[Boolean] mustBe true
      (content \ "fileContent").as[String] mustBe "mockContent"
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
      mockLogger.containsA[FileFoundResponse](_.requestId mustBe a[String])
    }

    "respond with valid body content and no file content upon valid create file attempt" in {
      prepareBodyParserForRequestFileBeingCreated

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      val content = contentAsJson(result)
      (content \ "requestId").as[String] mustBe mockRequestIdResponseFileBeingCreated
      (content \ "created").as[Boolean] mustBe false
      !(content \ "fileContent").isDefined
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
      mockLogger.containsA[FileIsBeingCreatedResponse](_.requestId mustBe a[String])
    }

    "respond with 429 upon valid create file attempt with requests limit reached" in {
      prepareBodyParserForRequestLimitReached

      val fileController = buildController
      val result = fileController.postCreateFile() apply FakeRequest()

      status(result) mustBe TOO_MANY_REQUESTS
      mockLogger.containsA[CreateFileRequestReceived](_.requestId mustBe a[String])
      mockLogger.containsA[TooManyRequestsResponse](_.requestId mustBe a[String])
    }
  }

  private def buildController = {
    new FileController(
      actorSystem.spawn(rateLimitingBehaviour()),
      actorSystem.spawn(filesServiceBehaviour()),
      mockBodyParser,
      stubControllerComponents(),
      mockLogger
    )
  }

  private def prepareBodyParserForRequestAllowed = prepareBodyParserForRequest(mockRequestIdResponseAllowed)

  private def prepareBodyParserForRequestLimitReached = prepareBodyParserForRequest(mockRequestIdResponseLimitReached)

  private def prepareBodyParserForRequestFileBeingCreated = prepareBodyParserForRequest(
    mockRequestIdResponseFileBeingCreated
  )

  private def prepareBodyParserForRequest(requestId: String) =
    (() => mockBodyParser.parseCreateFileRequest())
      .expects()
      .returning(stubBodyParser[CreateFileRequest](CreateFileRequest(requestId)))

  private def rateLimitingBehaviour() = Behaviors.receiveMessage[RequestsRateLimitingActor.Command] {
    case RequestsRateLimitingActor.RequestResource(r, sender) =>
      if (r == mockRequestIdResponseAllowed || r == mockRequestIdResponseFileBeingCreated) {
        sender ! RequestsRateLimitingActor.Allowed()
        Behaviors.same
      } else {
        sender ! RequestsRateLimitingActor.LimitReached()
        Behaviors.same
      }
    case _ => Behaviors.empty
  }

  private def filesServiceBehaviour() = Behaviors.receiveMessage[FilesServiceActor.Command] {
    case FilesServiceActor.GetOrCreateFile(r, sender) =>
      if (r == mockRequestIdResponseAllowed) {
        sender ! FilesServiceActor.FileFound("mockContent")
        Behaviors.same
      } else {
        sender ! FilesServiceActor.FileIsBeingCreated()
        Behaviors.same
      }
    case _ => Behaviors.empty
  }
}
