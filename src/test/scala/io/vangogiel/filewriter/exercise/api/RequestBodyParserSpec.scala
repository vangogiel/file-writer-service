package io.vangogiel.filewriter.exercise.api

import akka.actor.ActorSystem
import akka.util.ByteString
import org.scalatest.concurrent.Eventually
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers.{ contentAsJson, defaultAwaitTimeout, status, stubPlayBodyParsers }

import scala.concurrent.ExecutionContext.Implicits.global

class RequestBodyParserSpec extends PlaySpec with Eventually {
  implicit val actorSystem: ActorSystem = ActorSystem()

  val bodyParser = new RequestBodyParser(stubPlayBodyParsers)

  "Parsing a request body into a LoginRequest" when {
    "the body is valid and the request" should {
      "return a valid LoginRequest" in {
        val outcome = bodyParser
          .parseCreateFileRequest()
          .apply(FakeRequest())
          .run(ByteString(Json.toJson(createFileRequest).toString()))
          .map(_.toOption.get)

        eventually(outcome.value.get.get mustBe createFileRequest)
      }
    }

    "the body is not Json and the request" should {
      val outcome = bodyParser
        .parseCreateFileRequest()
        .apply(FakeRequest().withBody("not json"))
        .run()
        .map(_.left.getOrElse(Results.ImATeapot))

      "return BadRequest in the response" in {
        status(outcome) mustBe BAD_REQUEST
      }

      "return an error stating that the body is not Json" in {
        (contentAsJson(outcome) \ "status").as[String] mustBe "error"
        (contentAsJson(outcome) \ "error" \ 0 \ "errorName").as[String] mustBe "bodyIsNotJson"
      }
    }

    "the body is empty" should {
      val outcome = bodyParser
        .parseCreateFileRequest()
        .apply(FakeRequest())
        .run()
        .map(_.left.getOrElse(Results.ImATeapot))

      "return BadRequest in the response" in {
        status(outcome) mustBe BAD_REQUEST
      }

      "return an error stating that the body is empty" in {
        (contentAsJson(outcome) \ "status").as[String] mustBe "error"
        (contentAsJson(outcome) \ "error" \ 0 \ "errorName").as[String] mustBe "bodyIsEmpty"
      }
    }

    "the body doest not match schema" should {
      val outcome = bodyParser
        .parseCreateFileRequest()
        .apply(FakeRequest())
        .run(ByteString("{\"request\": \"123\"}"))
        .map(_.left.getOrElse(Results.ImATeapot))

      "return BadRequest in the response" in {
        status(outcome) mustBe BAD_REQUEST
      }

      "return an error stating that the body is empty" in {
        (contentAsJson(outcome) \ "status").as[String] mustBe "error"
        (contentAsJson(outcome) \ "error" \ 0 \ "errorName").as[String] mustBe "bodyDoesNotMatchSchema"
      }
    }
  }

  def createFileRequest: CreateFileRequest = {
    CreateFileRequest(
      "mockRequestId"
    )
  }
}
