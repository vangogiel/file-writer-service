package myraindrop.exercise.logger

import org.scalatest.Inspectors
import org.scalatestplus.play.PlaySpec
import play.api.MarkerContext
import play.api.libs.json.OWrites

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect.runtime.universe

class TestLogger extends PlaySpec with TypedLogger with Inspectors {
  private val loggingEvents: mutable.Buffer[Object] = mutable.Buffer.empty

  override def info[T <: Loggable: OWrites: universe.TypeTag](event: T)(implicit mc: MarkerContext): Unit = {
    loggingEvents += event
  }

  def containsA[T: ClassTag](assertions: T => Unit): Unit = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    forAtLeast(1, loggingEvents.filter(event => clazz.isInstance(event)).map(_.asInstanceOf[T]))(assertions)
  }

  override def error[T <: LoggableError: OWrites: universe.TypeTag](event: T)(implicit mc: MarkerContext): Unit = {
    loggingEvents += event
  }
}
