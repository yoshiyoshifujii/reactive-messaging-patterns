package com.github.yoshiyoshifujii.reactive_messaging_patterns.recipient_list

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

final case class RetailItem(itemId: String, retailPrice: Double)

object RecipientList {

  final case class PriceQuote(
      rfqId: String,
      itemId: String,
      retailPrice: Double,
      discountPrice: Double
  )

  sealed trait Command

  final case class RequestForQuotation(
      rfqId: String,
      retailItems: Seq[RetailItem],
      priceQuoteRef: ActorRef[PriceQuote]
  ) extends Command {
    lazy val totalRetailPrice: Double = retailItems.map(_.retailPrice).sum
  }

  final case class PriceQuoteInterest(
      path: String,
      quoteProcessor: ActorRef[Command],
      lowTotalRetail: Double,
      highTotalRetail: Double
  ) extends Command

  final case class RequestPriceQuote(
      rfqId: String,
      itemId: String,
      retailPrice: Double,
      orderTotalRetailPrice: Double,
      reply: ActorRef[PriceQuote]
  ) extends Command {

    private def discount(discountPercentage: Double => Double): Double =
      discountPercentage(orderTotalRetailPrice) * retailPrice

    private def discountPrice(discountPercentage: Double => Double): Double =
      retailPrice - discount(discountPercentage)

    def toPriceQuote(discountPercentage: Double => Double): PriceQuote =
      PriceQuote(rfqId, itemId, retailPrice, discountPrice(discountPercentage))
  }

  object MountaineeringSuppliesOrderProcessor {

    def behavior: Behavior[Command] =
      Behaviors.setup { context =>
        val interestRegistry = scala.collection.mutable.Map.empty[String, PriceQuoteInterest]

        Behaviors.receiveMessage {
          case interest: PriceQuoteInterest =>
            interestRegistry(interest.path) = interest
            Behaviors.same
          case rfq: RequestForQuotation =>
            for {
              interest <- interestRegistry.values
              if rfq.totalRetailPrice >= interest.lowTotalRetail
              if rfq.totalRetailPrice <= interest.highTotalRetail
            } {
              val recipient = interest.quoteProcessor
              rfq.retailItems.foreach { retailItem =>
                context.log.info(
                  "OrderProcessor: " + rfq.rfqId + " item: " + retailItem.itemId + " to: " + recipient.path.toString
                )
                recipient ! RequestPriceQuote(
                  rfq.rfqId,
                  retailItem.itemId,
                  retailItem.retailPrice,
                  rfq.totalRetailPrice,
                  rfq.priceQuoteRef
                )
              }
            }
            Behaviors.same
          case message =>
            context.log.info(s"OrderProcessor: received unexpected message: $message")
            Behaviors.same
        }
      }
  }

  object BudgetHikersPriceQuotes {

    def behavior(interestRegistrar: ActorRef[Command]): Behavior[Command] =
      Behaviors.setup { context =>
        interestRegistrar ! PriceQuoteInterest(context.self.path.toString, context.self, 1.00, 1_000.00)

        Behaviors.receiveMessage {
          case rpq: RequestPriceQuote =>
            rpq.reply ! rpq.toPriceQuote(discountPercentage)
            Behaviors.same
          case message =>
            context.log.info(s"BudgetHikersPriceQuotes: received unexpected message: $message")
            Behaviors.same
        }
      }

    private def discountPercentage(orderTotalRetailPrice: Double): Double = {
      if (orderTotalRetailPrice <= 100.00) 0.02
      else if (orderTotalRetailPrice <= 399.99) 0.03
      else if (orderTotalRetailPrice <= 499.99) 0.05
      else if (orderTotalRetailPrice <= 799.99) 0.07
      else 0.075
    }
  }

  object HighSierraPriceQuotes {

    def behavior(interestRegistrar: ActorRef[Command]): Behavior[Command] =
      Behaviors.setup { context =>
        interestRegistrar ! PriceQuoteInterest(context.self.path.toString, context.self, 100.00, 10000.00)

        Behaviors.receiveMessage {
          case rpq: RequestPriceQuote =>
            rpq.reply ! rpq.toPriceQuote(discountPercentage)
            Behaviors.same
          case message =>
            context.log.info(s"HighSierraPriceQuotes: received unexpected message: $message")
            Behaviors.same
        }
      }

    private def discountPercentage(orderTotalRetailPrice: Double): Double = {
      if (orderTotalRetailPrice <= 150.00) 0.015
      else if (orderTotalRetailPrice <= 499.99) 0.02
      else if (orderTotalRetailPrice <= 999.99) 0.03
      else if (orderTotalRetailPrice <= 4999.99) 0.04
      else 0.05
    }
  }

  object MountainAscentPriceQuotes {

    def behavior(interestRegistrar: ActorRef[Command]): Behavior[Command] =
      Behaviors.setup { context =>
        interestRegistrar ! PriceQuoteInterest(context.self.path.toString, context.self, 70.00, 5000.00)

        Behaviors.receiveMessage {
          case rpq: RequestPriceQuote =>
            rpq.reply ! rpq.toPriceQuote(discountPercentage)
            Behaviors.same
          case message =>
            context.log.info(s"MountainAscentPriceQuotes: received unexpected message: $message")
            Behaviors.same
        }
      }

    private def discountPercentage(orderTotalRetailPrice: Double) = {
      if (orderTotalRetailPrice <= 99.99) 0.01
      else if (orderTotalRetailPrice <= 199.99) 0.02
      else if (orderTotalRetailPrice <= 499.99) 0.03
      else if (orderTotalRetailPrice <= 799.99) 0.04
      else if (orderTotalRetailPrice <= 999.99) 0.045
      else if (orderTotalRetailPrice <= 2999.99) 0.0475
      else 0.05
    }

  }

  object PinnacleGearPriceQuotes {

    def behavior(interestRegistrar: ActorRef[Command]): Behavior[Command] =
      Behaviors.setup { context =>
        interestRegistrar ! PriceQuoteInterest(context.self.path.toString, context.self, 250.00, 500000.00)

        Behaviors.receiveMessage {
          case rpq: RequestPriceQuote =>
            rpq.reply ! rpq.toPriceQuote(discountPercentage)
            Behaviors.same
          case message =>
            context.log.info(s"PinnacleGearPriceQuotes: received unexpected message: $message")
            Behaviors.same
        }
      }

    private def discountPercentage(orderTotalRetailPrice: Double) = {
      if (orderTotalRetailPrice <= 299.99) 0.015
      else if (orderTotalRetailPrice <= 399.99) 0.0175
      else if (orderTotalRetailPrice <= 499.99) 0.02
      else if (orderTotalRetailPrice <= 999.99) 0.03
      else if (orderTotalRetailPrice <= 1199.99) 0.035
      else if (orderTotalRetailPrice <= 4999.99) 0.04
      else if (orderTotalRetailPrice <= 7999.99) 0.05
      else 0.06
    }
  }

  object RockBottomOuterwearPriceQuotes {

    def behavior(interestRegistrar: ActorRef[Command]): Behavior[Command] =
      Behaviors.setup { context =>
        interestRegistrar ! PriceQuoteInterest(context.self.path.toString, context.self, 0.50, 7500.00)

        Behaviors.receiveMessage {
          case rpq: RequestPriceQuote =>
            rpq.reply ! rpq.toPriceQuote(discountPercentage)
            Behaviors.same
          case message =>
            context.log.info(s"RockBottomOuterwearPriceQuotes: received unexpected message: $message")
            Behaviors.same
        }
      }

    private def discountPercentage(orderTotalRetailPrice: Double) = {
      if (orderTotalRetailPrice <= 100.00) 0.015
      else if (orderTotalRetailPrice <= 399.99) 0.02
      else if (orderTotalRetailPrice <= 499.99) 0.03
      else if (orderTotalRetailPrice <= 799.99) 0.04
      else if (orderTotalRetailPrice <= 999.99) 0.05
      else if (orderTotalRetailPrice <= 2999.99) 0.06
      else if (orderTotalRetailPrice <= 4999.99) 0.07
      else if (orderTotalRetailPrice <= 5999.99) 0.075
      else 0.08
    }
  }
}
