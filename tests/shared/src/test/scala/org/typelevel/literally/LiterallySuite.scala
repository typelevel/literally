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

import munit.FunSuite

class LiterallySuite extends FunSuite {

  import org.typelevel.literally.examples.{ShortString, Port}
  import org.typelevel.literally.examples.literals._

  test("short string construction") {
    assertEquals(short"asdf", ShortString.fromString("asdf").get)
  }

  test("short string literal prevents invalid construction") {
    compileErrors("""short"asdfasdfasdf"""")
  }

  test("port construction") {
    assertEquals(port"8080", Port.fromInt(8080).get)
  }

  test("port literal prevents invalid construction") {
    assert(compileErrors("""port"asdf"""").nonEmpty)
    assert(compileErrors("""port"-1"""").nonEmpty)
    assert(compileErrors("""port"100000"""").nonEmpty)
  }

  final val intOne = 1
  final val stringTwo = "2"
  final val doubleThree = 3.0
  final val charFour = '4'

  test("literal allows interpolation of primitive constants") {
    assertEquals(port"1$stringTwo", Port.fromInt(12).get)
    assertEquals(port"$intOne", Port.fromInt(1).get)
    assertEquals(short"$doubleThree", ShortString.fromString("3.0").get)
    assertEquals(port"1${stringTwo}3$charFour", Port.fromInt(1234).get)
    assertEquals(port"${1}${"2"}${'3'}", Port.fromInt(123).get)
  }

  final val onetwo = (1, 2)
  test("literal disallows interpolation of non-primivive constants") {
    assert(clue(compileErrors("""port"$onetwo"""")).contains("interpolated literal values must be primitive types"))
  }

  test("literal disallows interpolation of non-constant values") {
    assert(clue(compileErrors("""val aString = "1"; port"$aString"""")).contains("interpolated values need to be compile-time constants"))
  }

}
