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

import org.typelevel.literally.Literally

object literals:      
  extension (inline ctx: StringContext)
    inline def short(inline args: Any*): ShortString =
      ${ShortStringLiteral('ctx, 'args)}

    inline def port(inline args: Any*): Port =
      ${PortLiteral('ctx, 'args)}

  object ShortStringLiteral extends Literally[ShortString]:
    def validate(s: String)(using Quotes) =
      if s.length <= ShortString.MaxLength then Right('{ShortString.unsafeFromString(${Expr(s)})}) 
      else Left(s"ShortString must be <= ${ShortString.MaxLength} characters")

  object PortLiteral extends Literally[Port]:
    def validate(s: String)(using Quotes) =
      s.toIntOption.flatMap(Port.fromInt) match
        case None => Left(s"invalid port - must be integer between ${Port.MinValue} and ${Port.MaxValue}")
        case Some(_) => Right('{Port.fromInt(${Expr(s)}.toInt).get})