package com.github.yoshiyoshifujii.reactive_messaging_patterns.idempotent_receiver

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

object RiskAssessment {

  final case class RiskClassified(classification: String)

  sealed trait Command
  final case class AttachDocument(documentText: String)          extends Command
  final case class ClassifyRisk(reply: ActorRef[RiskClassified]) extends Command

  private final case class Document(documentText: Option[String]) {
    if (documentText.isDefined) {
      val text = documentText.get
      if (text == null || text.trim.isEmpty) {
        throw new IllegalStateException("Document must have text.")
      }
    }

    def determineClassification: String = {
      val text = documentText.get.toLowerCase

      if (text.contains("low")) "Low"
      else if (text.contains("medium")) "Medium"
      else if (text.contains("high")) "High"
      else "Unknown"
    }

  }

  private def documented(document: Document): Behavior[Command] =
    Behaviors.receiveMessage {
      case _: AttachDocument =>
        Behaviors.same
      case ClassifyRisk(replyTo) =>
        replyTo ! RiskClassified(document.determineClassification)
        Behaviors.same
    }

  private def undocumented: Behavior[Command] =
    Behaviors.receiveMessage {
      case attachment: AttachDocument =>
        documented(Document(Some(attachment.documentText)))
      case ClassifyRisk(replyTo) =>
        replyTo ! RiskClassified("Unknown")
        Behaviors.same
    }

  def behavior: Behavior[Command] = undocumented
}
