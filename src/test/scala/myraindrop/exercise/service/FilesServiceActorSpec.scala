package myraindrop.exercise.service

import akka.actor.testkit.typed.scaladsl.{ ActorTestKit, ScalaTestWithActorTestKit }
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.util.Timeout
import myraindrop.exercise.service.FilesServiceActor.{
  FileIsBeingCreated,
  FileFound,
  GetOrCreateFile,
  RemoveCompleteProcess
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Millis, Seconds, Span }
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.collection.immutable.List
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{ DurationInt, FiniteDuration }

class FilesServiceActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with MockFactory with Eventually {
  val actorSystem: ActorTestKit = ActorTestKit()
  override implicit val timeout: Timeout = Timeout(5.seconds)
  implicit override val patienceConfig =
    PatienceConfig(timeout = scaled(Span(6, Seconds)), interval = scaled(Span(900, Millis)))

  val mockFilesResource = mock[FilesResource]
  val mockFilesAsyncFileCreator = mock[FilesAsyncFileCreator]

  "FilesServiceActor" should {
    "respond with FileBeingCreated to read a file that is already being created" in {
      val testProbe = testKit.createTestProbe[FilesServiceActor.Response]()
      val subject = testKit.spawn(
        FilesServiceActor.apply(List("mockRequestId"), mockFilesResource, mockFilesAsyncFileCreator, actorSystem.system)
      )

      subject ! GetOrCreateFile("mockRequestId", testProbe.ref)

      testProbe.expectMessageType[FileIsBeingCreated]
    }

    "respond with FileFound to read an existing file content" in {
      (mockFilesResource
        .checkFilesExists(_: String))
        .expects(*)
        .returning(true)
      (mockFilesResource
        .readFileContent(_: String))
        .expects(*)
        .returning("12345")
      val testProbe = testKit.createTestProbe[FilesServiceActor.Response]()
      val subject =
        testKit.spawn(FilesServiceActor.apply(List(), mockFilesResource, mockFilesAsyncFileCreator, actorSystem.system))

      subject ! GetOrCreateFile("mockRequestId", testProbe.ref)

      testProbe.expectMessageType[FileFound]
    }

    "respond with FileBeingCreated to an attempt to retrieve non existing file" in {
      (mockFilesResource
        .checkFilesExists(_: String))
        .expects(*)
        .returning(false)
      (mockFilesAsyncFileCreator
        .runAsync(_: String, _: FilesResource, _: ActorRef[FilesServiceActor.Command], _: ActorSystem[Nothing])(
          _: ExecutionContext
        ))
        .expects(*, *, *, *, *)
        .once()
      val testProbe = testKit.createTestProbe[FilesServiceActor.Response]()
      val subject =
        testKit.spawn(FilesServiceActor.apply(List(), mockFilesResource, mockFilesAsyncFileCreator, actorSystem.system))

      subject ! GetOrCreateFile("mockRequestId", testProbe.ref)

      testProbe.expectMessageType[FileIsBeingCreated]
    }
  }

  "FilesAsyncFileCreator" should {
    "schedule to create a file in 5s" in {
      (mockFilesResource
        .writeNewFile(_: String))
        .expects(*)
        .once()
      val testProbe = testKit.createTestProbe[FilesServiceActor.Command]()
      new FilesAsyncFileCreator().runAsync("mock", mockFilesResource, testProbe.ref, actorSystem.system)

      testProbe.expectMessageType[RemoveCompleteProcess](FiniteDuration(6, TimeUnit.SECONDS))
    }
  }
}
