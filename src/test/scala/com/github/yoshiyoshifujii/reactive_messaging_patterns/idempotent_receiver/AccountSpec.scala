package com.github.yoshiyoshifujii.reactive_messaging_patterns.idempotent_receiver

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

class AccountSpec extends AnyFreeSpec with BeforeAndAfterAll {
  private val testKit: ActorTestKit = ActorTestKit()

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  "Account" - {

    "success" in {
      val accountId1 = AccountId()

      val accountRef = testKit.spawn(Account.behavior(accountId1))
      val probe      = testKit.createTestProbe[Account.AccountBalance]

      val deposit1 = Account.Deposit(TransactionId(), Money(100))
      accountRef ! deposit1
      accountRef ! Account.QueryBalance(probe.ref)
      probe.expectMessage(Account.AccountBalance(accountId1, Money(100)))

      // 重複実行
      accountRef ! deposit1

      accountRef ! Account.Deposit(TransactionId(), Money(20))
      accountRef ! Account.QueryBalance(probe.ref)
      probe.expectMessage(Account.AccountBalance(accountId1, Money(120)))

      // 重複実行
      accountRef ! deposit1

      accountRef ! Account.Withdraw(TransactionId(), Money(50))
      accountRef ! Account.QueryBalance(probe.ref)
      probe.expectMessage(Account.AccountBalance(accountId1, Money(70)))

      // 重複実行
      accountRef ! deposit1

      accountRef ! Account.Deposit(TransactionId(), Money(70))
      accountRef ! Account.QueryBalance(probe.ref)
      probe.expectMessage(Account.AccountBalance(accountId1, Money(140)))

      // 重複実行
      accountRef ! deposit1

      accountRef ! Account.Withdraw(TransactionId(), Money(100))
      accountRef ! Account.QueryBalance(probe.ref)
      probe.expectMessage(Account.AccountBalance(accountId1, Money(40)))

      // 重複実行
      accountRef ! deposit1

      accountRef ! Account.Deposit(TransactionId(), Money(10))
      accountRef ! Account.QueryBalance(probe.ref)
      probe.expectMessage(Account.AccountBalance(accountId1, Money(50)))
    }

  }
}
