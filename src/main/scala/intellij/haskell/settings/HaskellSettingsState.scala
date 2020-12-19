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

package intellij.haskell.settings

object HaskellSettingsState {
  private def state = HaskellSettingsPersistentStateComponent.getInstance().getState

  def getReplTimeout: Integer = {
    state.replTimeout
  }

  def getHlintOptions: String = {
    state.hlintOptions
  }

  def useSystemGhc: Boolean = {
    state.useSystemGhc
  }

  def getNewProjectTemplateName: String = {
    state.newProjectTemplateName
  }

  def getCachePath: String = {
    state.cachePath
  }

  def isReformatCodeBeforeCommit: Boolean = {
    state.reformatCodeBeforeCommit
  }

  def setReformatCodeBeforeCommit(reformat: Boolean): Unit = {
    state.reformatCodeBeforeCommit = reformat
  }

  def isOptmizeImportsBeforeCommit: Boolean = {
    state.optimizeImportsBeforeCommit
  }

  def setOptimizeImportsBeforeCommit(optimize: Boolean): Unit = {
    state.optimizeImportsBeforeCommit = optimize
  }

  def customTools: Boolean = {
    state.customTools
  }

  def hlintPath: Option[String] = {
    Option.when(customTools && state.hlintPath.nonEmpty)(state.hlintPath)
  }

  def hooglePath: Option[String] = {
    Option.when(customTools && state.hooglePath.nonEmpty)(state.hooglePath)
  }

  def ormoluPath: Option[String] = {
    Option.when(customTools && state.ormoluPath.nonEmpty)(state.ormoluPath)
  }

  def stylishHaskellPath: Option[String] = {
    Option.when(customTools && state.stylishHaskellPath.nonEmpty)(state.stylishHaskellPath)
  }

  def useCustomTools: Boolean = {
    state.customTools
  }

  def getExtraStackArguments: Seq[String] = {
    Option.when(state.extraStackArguments.trim.nonEmpty)(state.extraStackArguments).map(_.split("""\s+""").toSeq).getOrElse(Seq())
  }

  def getDefaultGhcOptions: Seq[String] = {
    state.defaultGhcOptions.split(" ").map(_.trim)
  }
}
