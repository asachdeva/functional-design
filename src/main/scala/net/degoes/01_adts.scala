package net.degoes

import java.time.Instant
import java.{ util => ju }
import java.time.LocalDate
import scala.concurrent.duration.Duration

/*
 * INTRODUCTION
 *
 * Functional Design depends heavily on functional data modeling. Functional
 * data modeling is the task of creating precise, type-safe models of a given
 * domain using algebraic data types and generalized algebraic data types.
 *
 * In this section, you'll review basic functional domain modeling.
 */

/**
 * E-COMMERCE - EXERCISE SET 1
 *
 * Consider an e-commerce application that allows users to purchase products.
 */
object credit_card {

  /**
   * EXERCISE 1
   *
   * Using only sealed traits and case classes, create an immutable data model
   * of a credit card, which must have:
   *
   *  * Number
   *  * Name
   *  * Expiration date
   *  * Security code
   *
   * You want to have precise types as in not have number as String
   */
  final case class CreditCard(number: Long, name: String, expirationDate: LocalDate, securityCode: Int)

  /**
   * EXERCISE 2
   *
   * Using only sealed traits and case classes, create an immutable data model
   * of a product, which could be a physical product, such as a gallon of milk,
   * or a digital product, such as a book or movie, or access to an event, such
   * as a music concert or film showing.
   */
  sealed trait Product { self =>
    def price: BigDecimal
  }
  object Product {
    final case class Physical(price: BigDecimal) extends Product
    final case class Digital(price: BigDecimal)  extends Product
    final case class Event(price: BigDecimal)    extends Product
  }

  /**
   * EXERCISE 3
   *
   * Using only sealed traits and case classes, create an immutable data model
   * of a product price, which could be one-time purchase fee, or a recurring
   * fee on some regular interval.
   */
  sealed trait PricingScheme {
    def fee: BigDecimal
  }

  object PricingScheme {
    final case class OneTime(fee: BigDecimal)                       extends PricingScheme
    final case class Recurring(fee: BigDecimal, duration: Duration) extends PricingScheme
  }
}

/**
 * EVENT PROCESSING - EXERCISE SET 3
 *
 * Consider an event processing application, which processes events from both
 * devices, as well as users.
 */
object events {

  sealed trait Event {
    def id: Int
    def time: Instant
  }

  object Event {
    sealed trait UserEvent extends Event {
      def userName: String
    }

    object UserEvent {
      final case class Purchase(id: Int, item: String, price: Double, time: Instant, userName: String) extends UserEvent
      final case class AccountCreated(id: Int, val userName: String, val time: Instant)                extends UserEvent
    }

    sealed trait DeviceEvent extends Event {
      def deviceId: Int
    }

    object DeviceEvent {
      final case class SensorUpdated(id: Int, val deviceId: Int, val time: Instant, val reading: Option[Double])
          extends DeviceEvent
      final case class Activated(id: Int, val deviceId: Int, val time: Instant) extends DeviceEvent
    }
  }
  /**
   * EXERCISE
   *
   * Refactor the object-oriented data model in this section to a more
   * functional one, which uses only sealed traits and case classes.
   */
  // abstract class Event(val id: Int) {

  //   def time: Instant
  // }

  // // Events are either UserEvent (produced by a user) or DeviceEvent (produced by a device),
  // // please don't extend both it will break code!!!
  // trait UserEvent extends Event {
  //   def userName: String
  // }

  // // Events are either UserEvent (produced by a user) or DeviceEvent (produced by a device),
  // // please don't extend both it will break code!!!
  // trait DeviceEvent extends Event {
  //   def deviceId: Int
  // }

  // class SensorUpdated(id: Int, val deviceId: Int, val time: Instant, val reading: Option[Double])
  //     extends Event(id)
  //     with DeviceEvent

  // class DeviceActivated(id: Int, val deviceId: Int, val time: Instant) extends Event(id) with DeviceEvent

  // class UserPurchase(id: Int, val item: String, val price: Double, val time: Instant, val userName: String)
  //     extends Event(id)
  //     with UserEvent

  // class UserAccountCreated(id: Int, val userName: String, val time: Instant) extends Event(id) with UserEvent

}

/**
 * DOCUMENT EDITING - EXERCISE SET 4
 *
 * Consider a web application that allows users to edit and store documents
 * of some type (which is not relevant for these exercises).
 */
object documents {
  final case class UserId(identifier: String)
  final case class DocId(identifier: String)
  final case class DocContent(body: String)

  /**
   * EXERCISE 1
   *
   * Using only sealed traits and case classes, create a simplified but somewhat
   * realistic model of a Document.
   */
  final case class Document(userId: UserId, docId: DocId, body: DocContent)

  /**
   * EXERCISE 2
   *
   * Using only sealed traits and case classes, create a model of the access
   * type that a given user might have with respect to a document. For example,
   * some users might have read-only permission on a document.
   */
  sealed trait AccessType
  object AccessType {
    case object NoAccess    extends AccessType
    case object ReadAccess  extends AccessType
    case object WriteAccess extends AccessType
    case object OwnerAccess extends AccessType
  }

  /**
   * EXERCISE 3
   *
   * Using only sealed traits and case classes, create a model of the
   * permissions that a user has on a set of documents they have access to.
   * Do not store the document contents themselves in this model.
   */
  final case class DocPermissions(userId: UserId, permissions: Map[DocId, AccessType])
}

/**
 * BANKING - EXERCISE SET 5
 *
 * Consider a banking application that allows users to hold and transfer money.
 */
object bank {

  /**
   * EXERCISE 1
   *
   * Using only sealed traits and case classes, develop a model of a customer at a bank.
   */
  final case class Customer(name: String, customerId: Long)

  /**
   * EXERCISE 2
   *
   * Using only sealed traits and case classes, develop a model of an account
   * type. For example, one account type allows the user to write checks
   * against a given currency. Another account type allows the user to earn
   * interest at a given rate for the holdings in a given currency.
   */
  sealed trait AccountType {}

  object AccountType {
    final case class InterestChecking(interestRate: BigDecimal) extends AccountType
    case object PersonalChecking                                extends AccountType
    case class MoneyMarket(interestRate: BigDecimal)            extends AccountType
  }

  /**
   * EXERCISE 3
   *
   * Using only sealed traits and case classes, develop a model of a bank
   * account, including details on the type of bank account, holdings, customer
   * who owns the bank account, and customers who have access to the bank account.
   */
  final case class Account(customer: Customer, accountType: AccountType, holdings: BigDecimal, access: Set[Customer])
}

/**
 * STOCK PORTFOLIO - GRADUATION PROJECT
 *
 * Consider a web application that allows users to manage their portfolio of investments.
 */
object portfolio {

  /**
   * EXERCISE 1
   *
   * Using only sealed traits and case classes, develop a model of a stock
   * exchange. Ensure there exist values for NASDAQ and NYSE.
   */
  sealed trait Exchange
  object Exchange {
    case object NASDAQ extends Exchange
    case object NYSE   extends Exchange
  }

  /**
   * EXERCISE 2
   *
   * Using only sealed traits and case classes, develop a model of a currency
   * type.
   */
  sealed trait CurrencyType
  object CurrencyType {
    case object USD extends CurrencyType
    case object EUR extends CurrencyType
  }

  /**
   * EXERCISE 3
   *
   * Using only sealed traits and case classes, develop a model of a stock
   * symbol. Ensure there exists a value for Apple's stock (APPL).
   */
  final case class StockSymbol(ticker: String, exchange: Exchange, currency: CurrencyType)

  /**
   * EXERCISE 4
   *
   * Using only sealed traits and case classes, develop a model of a portfolio
   * held by a user of the web application.
   */
  final case class Portfolio(user: User, holdings: Map[StockSymbol, Int], value: BigDecimal)

  /**
   * EXERCISE 5
   *
   * Using only sealed traits and case classes, develop a model of a user of
   * the web application.
   */
  final case class User(name: String, userId: Long)

  /**
   * EXERCISE 6
   *
   * Using only sealed traits and case classes, develop a model of a trade type.
   * Example trade types might include Buy and Sell.
   */
  sealed trait TradeType
  object TradeType {
    case object Buy  extends TradeType
    case object Sell extends TradeType
  }

  /**
   * EXERCISE 7
   *
   * Using only sealed traits and case classes, develop a model of a trade,
   * which involves a particular trade type of a specific stock symbol at
   * specific prices.
   */
  final case class Trade(stockSymbol: StockSymbol, tradeType: TradeType, price: BigDecimal, user: User)
}
