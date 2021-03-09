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

import scala.quoted._

trait Literally[A]:
  type Quotes = scala.quoted.Quotes
  type Expr[A] = scala.quoted.Expr[A]
  val Expr = scala.quoted.Expr

  def validate(s: String)(using Quotes): Either[String, Expr[A]]

  def apply(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[A] =
    strCtxExpr.value match
      case Some(sc) => apply(sc.parts, argsExpr)
      case None =>
        quotes.reflect.report.error("StringContext args must be statically known")
        ???

  private def apply(parts: Seq[String], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[A] =
    if parts.size == 1 then
      val literal = parts.head
      validate(literal) match
        case Left(err) =>
          quotes.reflect.report.error(err)
          ???
        case Right(a) =>
          a
    else
      quotes.reflect.report.error("interpolation not supported", argsExpr)
      ???
