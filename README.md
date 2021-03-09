# Literally

Compile time validation of literal values built from strings.

Consider a type like `Port`:

```scala
case class Port private (value: Int)

object Port {
  val MinValue = 0
  val MaxValue = 65535

  def fromInt(i: Int): Option[Port] =
    if (i < MinValue || i > MaxValue) None else Some(new Port(i))
}
```

This library simplifies the definition of literal values which are validated at compilation time:

```scala
val p: Port = port"8080"
// p: Port = Port(8080)

val q: Port = port"100000"
// <console>:17: error: invalid port - must be integer between 0 and 65535
```

Validation is performed at compile time. This library provides the macro implementations for both Scala 2 and Scala 3 which powers custom string literals.

### Usage

Defining a custom string literal for a type `A` involves:
- Implementing an `org.typelevel.literally.Literally[A]` instance
- Defining an extension method on a `StringContext` which uses the defined `Literally[A]` instance

```scala
import scala.util.Try
import org.typelevel.literally.Literally

object literals:      
  extension (inline ctx: StringContext)
    inline def port(inline args: Any*): Port =
      ${PortLiteral('ctx, 'args)}

  object PortLiteral extends Literally[Port]:
    def validate(s: String)(using Quotes) =
      s.toIntOption.flatMap(Port.fromInt) match
        case None => Left(s"invalid port - must be integer between ${Port.MinValue} and ${Port.MaxValue}")
        case Some(_) => Right('{Port.fromInt(${Expr(s)}.toInt).get})
```

The same pattern is used for Scala 2, though the syntax for extension methods and macros are a bit different:

```scala
import scala.util.Try
import org.typelevel.literally.Literally

object literals {
  implicit class short(val sc: StringContext) extends AnyVal {
    def port(args: Any*): Port = macro PortLiteral.make
  }

  object PortLiteral extends Literally[Port] {
    def validate(c: Context)(s: String): Either[String, c.Expr[Port]] = {
      import c.universe.{Try => _, _}
      Try(s.toInt).toOption.flatMap(Port.fromInt) match {
        case None => Left(s"invalid port - must be integer between ${Port.MinValue} and ${Port.MaxValue}")
        case Some(_) => Right(c.Expr(q"Port.fromInt($s.toInt).get"))
      }
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Port] = apply(c)(args: _*)
  }
}
```

The `tests` directory in this project has more examples.


