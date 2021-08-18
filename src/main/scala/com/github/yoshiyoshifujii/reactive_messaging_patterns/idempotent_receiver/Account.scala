package com.github.yoshiyoshifujii.reactive_messaging_patterns.idempotent_receiver

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }

final case class AccountId(value: String)

object AccountId {
  var currentId = 0

  def apply(): AccountId = {
    currentId = currentId + 1
    AccountId(currentId.toString)
  }
}

final case class Money(value: Double) {
  def +(other: Money): Money = Money(value + other.value)
  def negative: Money        = Money(0 - value)
}

final case class TransactionId(value: String)

object TransactionId {
  var currentId = 0

  def apply(): TransactionId = {
    currentId = currentId + 1
    TransactionId(currentId.toString)
  }
}

final case class Transaction(transactionId: TransactionId, amount: Money)

object Account {

  final case class AccountBalance(accountId: AccountId, amount: Money)

  sealed trait Command
  final case class Deposit(transactionId: TransactionId, amount: Money)  extends Command
  final case class QueryBalance(reply: ActorRef[AccountBalance])         extends Command
  final case class Withdraw(transactionId: TransactionId, amount: Money) extends Command

  def behavior(accountId: AccountId): Behavior[Command] =
    Behaviors.setup { implicit context =>
      val transactions = scala.collection.mutable.Map.empty[TransactionId, Transaction]

      Behaviors.receiveMessage {
        case deposit: Deposit =>
          val transaction = Transaction(deposit.transactionId, deposit.amount)
          context.log.info(s"Deposit: $transaction")
          transactions += (deposit.transactionId -> transaction)
          Behaviors.same
        case withdraw: Withdraw =>
          val transaction = Transaction(withdraw.transactionId, withdraw.amount.negative)
          context.log.info(s"Withdraw: $transaction")
          transactions += (withdraw.transactionId -> transaction)
          Behaviors.same
        case QueryBalance(replyTo) =>
          replyTo ! calculateBalance(accountId, transactions)
          Behaviors.same
      }
    }

  private def calculateBalance(
      accountId: AccountId,
      transactions: scala.collection.mutable.Map[TransactionId, Transaction]
  )(implicit
      context: ActorContext[Command]
  ): AccountBalance = {
    val amount = transactions.values.foldLeft(Money(0)) { (acc, transaction) =>
      acc + transaction.amount
    }
    context.log.info(s"Balance: $amount")
    AccountBalance(accountId, amount)
  }

}
