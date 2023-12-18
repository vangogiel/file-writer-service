package io.vangogiel.filewriter.exercise.actor

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.util.Timeout
import org.scalatest.wordspec.AnyWordSpecLike

import scala.collection.immutable.Map
import scala.concurrent.duration.DurationInt

class RequestsRateLimitingActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("test-actor")
  override implicit val timeout: Timeout = Timeout(5.seconds)

  "RequestsRateActor" should {
    "respond with RequestAllowed to one request" in {
      val testProbe = testKit.createTestProbe[RequestsRateLimitingActor.Response]()
      val subject = testKit.spawn(RequestsRateLimitingActor.apply(Map.empty))

      subject ! RequestResource("mockRequestId", testProbe.ref)

      testProbe.expectMessageType[Allowed]
    }

    "respond with RequestAllowed to two identical requests within a second" in {
      val testProbe = testKit.createTestProbe[RequestsRateLimitingActor.Response]()
      val subject = testKit.spawn(RequestsRateLimitingActor.apply(Map()))

      subject ! RequestResource("mockRequestId", testProbe.ref)
      subject ! RequestResource("mockRequestId", testProbe.ref)

      testProbe.expectMessageType[Allowed]
      testProbe.expectMessageType[Allowed]
    }

    "respond with LimitReached to three identical requests within a second" in {
      val testProbe = testKit.createTestProbe[RequestsRateLimitingActor.Response]()
      val subject = testKit.spawn(RequestsRateLimitingActor.apply(Map()))

      val millsStart = System.currentTimeMillis()
      subject ! RequestResource("mockRequestId", testProbe.ref)
      subject ! RequestResource("mockRequestId", testProbe.ref)
      subject ! RequestResource("mockRequestId", testProbe.ref)
      val millsEnd = System.currentTimeMillis()

      assert(millsEnd - millsStart < 1000)
      testProbe.expectMessageType[Allowed]
      testProbe.expectMessageType[Allowed]
      testProbe.expectMessageType[LimitReached]
    }

    "respond with RequestAllowed to two identical requests within a second and a third after" in {
      val testProbe = testKit.createTestProbe[RequestsRateLimitingActor.Response]()
      val subject = testKit.spawn(RequestsRateLimitingActor.apply(Map()))

      subject ! RequestResource("mockRequestId", testProbe.ref)
      subject ! RequestResource("mockRequestId", testProbe.ref)
      Thread.sleep(1000)
      subject ! RequestResource("mockRequestId", testProbe.ref)

      testProbe.expectMessageType[Allowed]
      testProbe.expectMessageType[Allowed]
      testProbe.expectMessageType[Allowed]
    }
  }
}
