package myraindrop.exercise.service

import akka.actor.Cancellable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import myraindrop.exercise.service.FilesServiceActor.{ Command, RemoveCompleteProcess }

import java.util.concurrent.TimeUnit
import scala.collection.immutable.List
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object FilesServiceActor {
  sealed trait Command
  case class GetOrCreateFile(requestId: String, sender: ActorRef[Response]) extends Command
  case class RemoveCompleteProcess(requestId: String) extends Command

  sealed trait Response
  case class FileFound(fileContents: String) extends Response
  case class FileIsBeingCreated() extends Response

  def apply(
      listOfFilesInProgress: List[String],
      filesResource: FilesResource,
      filesAsyncFileCreator: FilesAsyncFileCreator,
      actorSystem: ActorSystem[Nothing]
  )(implicit
      ec: ExecutionContext
  ): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case GetOrCreateFile(requestId, sender) =>
        if (listOfFilesInProgress.contains(requestId)) {
          sender ! FileIsBeingCreated()
          Behaviors.same
        } else {
          if (filesResource.checkFilesExists(requestId)) {
            val fileContents = filesResource.readFileContent(requestId)
            sender ! FileFound(fileContents)
            Behaviors.same
          } else {
            filesAsyncFileCreator.runAsync(requestId, filesResource, context.self, actorSystem)
            sender ! FileIsBeingCreated()
            apply(listOfFilesInProgress :+ requestId, filesResource, filesAsyncFileCreator, actorSystem)
          }
        }
      case RemoveCompleteProcess(requestId) =>
        apply(
          listOfFilesInProgress.filterNot(e => e.equals(requestId)),
          filesResource,
          filesAsyncFileCreator,
          actorSystem
        )
    }
  }
}

class FilesAsyncFileCreator {
  def runAsync(
      name: String,
      filesResource: FilesResource,
      sender: ActorRef[Command],
      actorSystem: ActorSystem[Nothing]
  )(implicit
      ec: ExecutionContext
  ): Cancellable = {
    val cancellable = actorSystem.scheduler.scheduleOnce(
      FiniteDuration(5, TimeUnit.SECONDS),
      () => {
        filesResource.writeNewFile(name)
        sender ! RemoveCompleteProcess(name)
      }
    )
    actorSystem.scheduler.scheduleOnce(
      FiniteDuration(10, TimeUnit.SECONDS),
      () =>
        if (!cancellable.isCancelled) {
          cancellable.cancel()
          sender ! RemoveCompleteProcess(name)
        }
    )
  }
}
