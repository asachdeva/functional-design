package net.degoes

object workspace {
  //
  // Please join and say hello:
  //
  // CHAT ROOM:  https://gitter.im/jdegoes/functional-scala
  //
  // Please git clone and build:
  //
  // REPOSITORY: https://github.com/jdegoes/functional-design
  //
  // Daily Schedule:
  //
  //    START :
  //    LUNCH :
  //    END   :

  // Data class...pattern match constructor equals hashCode ...product types since you can insert zero or more fields...cartesian cross product...Product TYPES
  // These are only one of the things
  case class Person(name: String, age: Int)

  // Sum types -- modeled as sealed traits...number of constructors is fixed..hence sealed...
  sealed trait ShippingMethod
  object ShippingMethod {
    case object Fedex  extends ShippingMethod
    case object DHL    extends ShippingMethod
    case object Postal extends ShippingMethod
  }
  
}
