package io.vangogiel.filewriter.exercise.controller

import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }

class HealthController(cc: ControllerComponents) extends AbstractController(cc) {
  def default(): Action[AnyContent] = Action {
    Ok
  }
}
