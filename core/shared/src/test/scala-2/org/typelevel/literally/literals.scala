/*
 * Copyright 2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.literally.examples

import scala.util.Try
import org.typelevel.literally.Literally

object literals {
  implicit class short(val sc: StringContext) extends AnyVal {
    def short(args: Any*): ShortString = macro ShortStringLiteral.make
    def port(args: Any*): Port = macro PortLiteral.make
  }

  object ShortStringLiteral extends Literally[ShortString] {
    def validate(s: String): Option[String] =
      if (s.length > ShortString.MaxLength) None 
      else Some(s"ShortString must be <= ${ShortString.MaxLength} characters")

    def build(c: Context)(s: c.Expr[String]) = 
      c.universe.reify { ShortString.unsafeFromString(s.splice) }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[ShortString] = apply(c)(args: _*)
  }

  object PortLiteral extends Literally[Port] {
    def validate(s: String): Option[String] =
      Try(s.toInt).toOption.flatMap { i => 
        Port.fromInt(i) match {
          case None => Some(s"invalid port - must be integer between ${Port.MinValue} and ${Port.MaxValue}")
          case Some(_) => None
        }
      }

    def build(c: Context)(s: c.Expr[String]) =
      c.universe.reify { Port.fromInt(s.splice.toInt).get }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[Port] = apply(c)(args: _*)
  }
}
