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

package org.typelevel.literally

trait Literally[A] {
  type Context = scala.reflect.macros.blackbox.Context

  def validate(c: Context)(s: String): Either[String, c.Expr[A]]

  def apply(c: Context)(args: c.Expr[Any]*): c.Expr[A] = {
    import c.universe._
    identity(args)
    c.prefix.tree match {
      case Apply(_, List(Apply(_, (Literal(Constant(p: String))) :: Nil))) =>
        validate(c)(p) match {
          case Left(msg) => c.abort(c.enclosingPosition, msg)
          case Right(a) => a
        }
    }
  }
}