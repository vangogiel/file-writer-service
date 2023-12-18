package io.vangogiel.filewriter.exercise

import io.vangogiel.fileservice.exercise.controller.HealthController
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{ defaultAwaitTimeout, status, stubControllerComponents }

class HealthControllerSpec extends PlaySpec {
  val healthController = new HealthController(stubControllerComponents())

  "The home controller endpoint" should {
    "respond with OK" in {
      val result = healthController.default() apply FakeRequest()

      status(result) mustBe OK
    }
  }
}
