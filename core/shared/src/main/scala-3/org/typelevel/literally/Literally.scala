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
    apply(strCtxExpr.valueOrAbort.parts, argsExpr)

  private def apply(parts: Seq[String], argsExpr: Expr[Seq[Any]])(using q: Quotes): Expr[A] =
    import q.reflect.*

    val str = argsExpr.asTerm match
      case Inlined(_, _, Typed(Repeated(terms, _), _)) =>
        def termsAsStrings = terms.map { term =>
          val staticVal = if (term.tpe <:< TypeRepr.of[String]) term.asExprOf[String].value
            else if (term.tpe <:< TypeRepr.of[Int]) term.asExprOf[Int].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Double]) term.asExprOf[Double].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Float]) term.asExprOf[Float].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Long]) term.asExprOf[Long].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Short]) term.asExprOf[Short].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Byte]) term.asExprOf[Byte].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Char]) term.asExprOf[Char].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Boolean]) term.asExprOf[Boolean].value.map(_.toString)
            else if (term.tpe <:< TypeRepr.of[Unit]) Some("()")
            else report.errorAndAbort(s"interpolated literal values must be primitive types", term.pos)
          staticVal.getOrElse(report.errorAndAbort("interpolated values must be compile-time constants"))
        }
        parts.zipAll(termsAsStrings, "", "").map(_ + _).mkString
      case unknown =>
        report.errorAndAbort(s"unexpected error interpolating with literally, which didn't expect an interpolation tree in the form of $unknown", unknown.pos)

    validate(str) match
      case Left(err) => report.errorAndAbort(err)
      case Right(value) => value