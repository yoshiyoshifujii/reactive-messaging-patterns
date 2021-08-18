package com.github.yoshiyoshifujii.reactive_messaging_patterns.idempotent_receiver

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import com.github.yoshiyoshifujii.reactive_messaging_patterns.idempotent_receiver.Account.{AccountBalance, QueryBalance}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class AccountSpec extends AnyFreeSpec with BeforeAndAfterAll {
  private val testKit: ActorTestKit = ActorTestKit()

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  "Account" - {

    "success" in {
      val accountId1 = AccountId()

      val accountRef = testKit.spawn(Account.behavior(accountId1))
      val probe = testKit.createTestProbe[AccountBalance]

      val deposit1 = Account.Deposit(TransactionId(), Money(100))
      accountRef ! deposit1
      accountRef ! QueryBalance(probe.ref)
      probe.expectMessage(AccountBalance(accountId1, deposit1.amount))

    }

  }
}
