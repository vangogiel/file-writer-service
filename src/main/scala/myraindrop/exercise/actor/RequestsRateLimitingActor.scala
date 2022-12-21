package myraindrop.exercise.actor

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable.Map

case class Resource(var timeOfFirstRequest: Long, var numberOfRequests: Int)

object RequestsRateLimitingActor {
  sealed trait Command
  case class RequestResource(requestId: String, sender: ActorRef[Response]) extends Command
  case class RemoveComplete() extends Command

  sealed trait Response
  case class Allowed() extends Response
  case class LimitReached() extends Response

  def apply(mapOfRequestsInProgress: Map[String, Resource]): Behavior[Command] = Behaviors.receive { (_, message) =>
    message match {
      case RequestResource(requestId, sender) =>
        mapOfRequestsInProgress.get(requestId) match {
          case Some(resource) =>
            val timeNowMilliseconds = System.currentTimeMillis()
            if (timeNowMilliseconds - resource.timeOfFirstRequest < 1000 && resource.numberOfRequests > 1) {
              sender ! LimitReached()
              Behaviors.same
            } else if (timeNowMilliseconds - resource.timeOfFirstRequest < 1000) {
              sender ! Allowed()
              apply(mapOfRequestsInProgress + (requestId -> Resource(resource.timeOfFirstRequest, 2)))
            } else {
              sender ! Allowed()
              apply(mapOfRequestsInProgress + (requestId -> Resource(timeNowMilliseconds, 1)))
            }
          case None =>
            sender ! Allowed()
            apply(mapOfRequestsInProgress + (requestId -> Resource(System.currentTimeMillis(), 1)))
        }
      case RemoveComplete() =>
        val timeNowMilliseconds = System.currentTimeMillis()
        val incomplete = mapOfRequestsInProgress.filter(k => timeNowMilliseconds - k._2.timeOfFirstRequest < 1000)
        apply(incomplete)
    }
  }
}
