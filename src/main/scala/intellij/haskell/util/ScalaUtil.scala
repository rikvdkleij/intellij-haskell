/*
 * Copyright 2016 Rik van der Kleij
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

import com.intellij.openapi.util.{Computable, Condition}

object ScalaUtil {

  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    final def option[A](a: => A): Option[A] = if (b) Option(a) else None
    final def optionNot[A](a: => A): Option[A] = if (b) None else Option(a)
  }

  def runnable(f: => Unit) = new Runnable {
    override def run(): Unit = f
  }

  def computable[A](f: => A) = new Computable[A] {
    override def compute(): A = f
  }

  def condition[A](f: A => Boolean) = new Condition[A] {
    override def value(t: A): Boolean = f(t)
  }
}
