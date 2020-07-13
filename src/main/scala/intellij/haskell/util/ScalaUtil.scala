/*
 * Copyright 2014-2020 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.util

import java.util.concurrent.Callable

import com.intellij.openapi.util.{Computable, Condition}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ScalaUtil {

  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Option(a) else None

    final def optionNot[A](a: => A): Option[A] = if (b) None else Option(a)
  }

  def runnable(f: => Unit): Runnable = {
    () => f
  }

  def computable[A](f: => A): Computable[A] = {
    () => f
  }

  def callable[A](f: => A): Callable[A] = {
    () => f
  }

  def condition[A](f: A => Boolean): Condition[A] = {
    t: A => f(t)
  }

  def maxsBy[A, B](xs: Iterable[A])(f: A => B)(implicit cmp: Ordering[B]): Iterable[A] = {
    val maxElems = mutable.Map[A, B]()

    for (elem <- xs) {
      val fx = f(elem)
      val removeKeys = maxElems.filter({ case (_, v) => cmp.gt(fx, v) }).keys
      removeKeys.map(maxElems.remove)

      maxElems.put(elem, fx)
    }
    maxElems.keys
  }

  def linesToMap(lines: Seq[String]): Map[String, String] = {
    val linePerKey = lines.foldLeft(ListBuffer[String]()) { case (xs, s) =>
      if (s.startsWith("  ")) {
        xs.update(xs.length - 1, xs.last + s)
        xs
      } else xs.+=(s)
    }

    linePerKey.flatMap(x => {
      val keyValuePair = x.split(": ", 2)
      if (keyValuePair.size == 2) {
        Some(keyValuePair(0), keyValuePair(1).trim)
      } else {
        None
      }
    }).toMap
  }
}
