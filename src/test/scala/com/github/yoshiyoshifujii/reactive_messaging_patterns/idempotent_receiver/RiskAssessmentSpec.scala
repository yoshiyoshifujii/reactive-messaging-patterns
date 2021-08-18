package com.github.yoshiyoshifujii.reactive_messaging_patterns.idempotent_receiver

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class RiskAssessmentSpec extends AnyFreeSpec with BeforeAndAfterAll {

  private val testKit: ActorTestKit = ActorTestKit()

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  "RiskAssessment" - {

    "success" in {

      val riskAssessmentRef = testKit.spawn(RiskAssessment.behavior)
      val probe             = testKit.createTestProbe[RiskAssessment.RiskClassified]

      riskAssessmentRef ! RiskAssessment.ClassifyRisk(probe.ref)
      probe.expectMessage(RiskAssessment.RiskClassified("Unknown"))

      riskAssessmentRef ! RiskAssessment.AttachDocument("This is a HIGH risk.")

      riskAssessmentRef ! RiskAssessment.ClassifyRisk(probe.ref)
      probe.expectMessage(RiskAssessment.RiskClassified("High"))
    }

  }
}
