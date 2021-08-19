package com.github.yoshiyoshifujii.reactive_messaging_patterns.recipient_list

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class RecipientListSpec extends AnyFreeSpec with BeforeAndAfterAll {
  private val testKit: ActorTestKit = ActorTestKit()

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  import RecipientList._

  "RecipientList" - {

    "success" in {
      val probe = testKit.createTestProbe[PriceQuote]

      val orderProcessor = testKit.spawn(MountaineeringSuppliesOrderProcessor.behavior, "orderProcessor")
      testKit.spawn(BudgetHikersPriceQuotes.behavior(orderProcessor), "budgetHikers")
      testKit.spawn(HighSierraPriceQuotes.behavior(orderProcessor), "highSierra")
      testKit.spawn(MountainAscentPriceQuotes.behavior(orderProcessor), "mountainAscent")
      testKit.spawn(PinnacleGearPriceQuotes.behavior(orderProcessor), "pinnacleGear")
      testKit.spawn(RockBottomOuterwearPriceQuotes.behavior(orderProcessor), "rockBottomOuterwear")

      orderProcessor ! RequestForQuotation(
        "123",
        Vector(
          RetailItem("1", 29.95),
          RetailItem("2", 99.95),
          RetailItem("3", 14.95)
        ),
        probe.ref
      )
      val result1 = probe.expectMessageType[PriceQuote]
      println(s"0 - ${result1}")
      assert(result1.rfqId === "123")
      assert(result1.itemId === "1")
      assert(result1.retailPrice === 29.95)
      assert(result1.discountPrice === 29.351)

      orderProcessor ! RequestForQuotation(
        "125",
        Vector(
          RetailItem("4", 39.99),
          RetailItem("5", 199.95),
          RetailItem("6", 149.95),
          RetailItem("7", 724.99)
        ),
        probe.ref
      )
      probe.expectMessage(PriceQuote("123", "2", 99.95, 97.95100000000001))

      orderProcessor ! RequestForQuotation(
        "129",
        Vector(
          RetailItem("8", 119.99),
          RetailItem("9", 499.95),
          RetailItem("10", 519.00),
          RetailItem("11", 209.50)
        ),
        probe.ref
      )
      probe.expectMessage(PriceQuote("123", "3", 14.95, 14.651))

      orderProcessor ! RequestForQuotation(
        "135",
        Vector(
          RetailItem("12", 0.97),
          RetailItem("13", 9.50),
          RetailItem("14", 1.99)
        ),
        probe.ref
      )
      probe.expectMessage(PriceQuote("123", "1", 29.95, 29.351))

      orderProcessor ! RequestForQuotation(
        "140",
        Vector(
          RetailItem("15", 107.50),
          RetailItem("16", 9.50),
          RetailItem("17", 599.99),
          RetailItem("18", 249.95),
          RetailItem("19", 789.99)
        ),
        probe.ref
      )
      probe.expectMessage(PriceQuote("123", "2", 99.95, 97.95100000000001))
    }
  }
}
