package io.vangogiel.filewriter.exercise.actor

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.DurationInt

object RequestRateEvictionActor {
  sealed trait Command
  case class StartScheduler() extends Command
  case class RequestEviction(
      jwtSessionsActor: ActorRef[RequestsRateLimitingActor.Command]
  ) extends Command

  def apply(
      requestsRateLimitingActor: ActorRef[RequestsRateLimitingActor.Command]
  ): Behavior[Command] = {
    Behaviors.setup { _ =>
      scheduleTask(requestsRateLimitingActor)
    }
  }

  private def scheduleTask(
      requestsRateLimitingActor: ActorRef[RequestsRateLimitingActor.Command]
  ): Behaviors.Receive[Command] = {
    Behaviors.receivePartial { _ =>
      Behaviors.withTimers[Command] { timers =>
        timers.startTimerAtFixedRate(
          msg = RequestEviction(requestsRateLimitingActor),
          initialDelay = 0.milliseconds,
          interval = 1000.milliseconds
        )
        commandToBeScheduled()
      }
    }
  }

  private def commandToBeScheduled(): Behaviors.Receive[Command] = {
    Behaviors.receiveMessagePartial { case RequestEviction(requestsRateLimitingActor) =>
      requestsRateLimitingActor ! RemoveComplete()
      Behaviors.same
    }
  }
}
