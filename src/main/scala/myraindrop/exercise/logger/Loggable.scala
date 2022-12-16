package myraindrop.exercise.logger

sealed trait Loggable {
  def message: String
}
