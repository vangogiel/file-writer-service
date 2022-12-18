package myraindrop.exercise.actor

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable.Map

case class Resource(var timeOfReceivingLatestRequest: Long, var numberOfRequests: Int)

object RequestsRateLimitingActor {
  sealed trait Command
  case class RequestResource(requestId: String, sender: ActorRef[Response]) extends Command

  sealed trait Response
  case class Allowed() extends Response
  case class LimitReached() extends Response

  def apply(mapOfRequestsInProgress: Map[String, Resource]): Behavior[Command] = Behaviors.receive { (_, message) =>
    message match {
      case RequestResource(requestId, sender) =>
        mapOfRequestsInProgress.get(requestId) match {
          case Some(resource) =>
            val timeNowMilliseconds = System.currentTimeMillis()
            if (timeNowMilliseconds - resource.timeOfReceivingLatestRequest < 1000 && resource.numberOfRequests > 1) {
              sender ! LimitReached()
              Behaviors.same
            } else if (timeNowMilliseconds - resource.timeOfReceivingLatestRequest < 1000) {
              sender ! Allowed()
              apply(mapOfRequestsInProgress + (requestId -> Resource(timeNowMilliseconds, 2)))
            } else {
              sender ! Allowed()
              apply(mapOfRequestsInProgress + (requestId -> Resource(timeNowMilliseconds, 1)))
            }
          case None =>
            sender ! Allowed()
            apply(mapOfRequestsInProgress + (requestId -> Resource(System.currentTimeMillis(), 1)))
        }
    }
  }
}
