/*
 * Copyright 2014-2019 Rik van der Kleij
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

package intellij.haskell.navigation

import com.intellij.navigation.{ChooseByNameContributor, ItemPresentation, NavigationItem}
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import intellij.haskell.external.component._
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.index.HaskellModuleNameIndex
import javax.swing.Icon

class HoogleByNameContributor extends ChooseByNameContributor {

  private final val DeclarationPattern = """([\w\.\-]+) (.*)""".r
  private final val ModulePattern = """module ([\w\.\-]+)""".r
  private final val PackagePattern = """package (.*)""".r

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    Iterable("a").toArray
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    val hooglePattern =
      if (includeNonProjectItems) {
        pattern
      } else {
        HaskellComponentsManager.findProjectPackageNames(project).map(_.foldLeft("")((s: String, pn: String) => s + s"+$pn")).map(_ + s" $pattern").getOrElse(pattern)
      }

    ProgressManager.checkCanceled()

    val navigationItems: Seq[NavigationItem] = HoogleComponent.runHoogle(project, hooglePattern, count = 25).getOrElse(Seq()).flatMap {
      case ModulePattern(moduleName) =>
        ProgressManager.checkCanceled()
        HaskellModuleNameIndex.findFilesByModuleName(project, moduleName) match {
          case Right(files) => files
          case _ => Seq()
        }
      case PackagePattern(packageName) =>
        ProgressManager.checkCanceled()
        Seq()
      case DeclarationPattern(moduleName, declaration) =>
        ProgressManager.checkCanceled()
        DeclarationLineUtil.findName(declaration) match {
          case Some(nameAndShortDeclaration) =>
            val identifiers = HaskellReference.findIdentifiersByModulesAndName(project, Seq(moduleName), nameAndShortDeclaration.name, prioIdInExpression = false).toOption.map(_._2).toSeq
            if (identifiers.isEmpty) {
              Seq()
            } else {
              identifiers.map(e => HaskellPsiUtil.findDeclarationElement(e).getOrElse(e)).map(d => createLibraryNavigationItem(d, moduleName))
            }
          case None => Seq()
        }
      case d =>
        ProgressManager.checkCanceled()
        Seq()
    }

    navigationItems.zipWithIndex.map({ case (item, i) => new NavigationItem {

      // Hack to display items in same order as given by Hoogle
      override def getName: String = {
        f"$pattern$i%06d" + " " + item.getName
      }

      override def getPresentation: ItemPresentation = item.getPresentation

      override def navigate(b: Boolean): Unit = item.navigate(b)

      override def canNavigate: Boolean = item.canNavigate

      override def canNavigateToSource: Boolean = item.canNavigateToSource
    }
    }).toArray
  }

  private def createLibraryNavigationItem(namedElement: NavigationItem, moduleNameFromHoogle: String): NavigationItem = {
    new NavigationItem {

      override def getName: String = namedElement.getName

      override def getPresentation: ItemPresentation = new ItemPresentation {
        override def getPresentableText: String = namedElement.getPresentation.getPresentableText

        override def getLocationString: String = namedElement.getPresentation.getLocationString + " -- " + moduleNameFromHoogle

        override def getIcon(unused: Boolean): Icon = namedElement.getPresentation.getIcon(unused)
      }

      override def navigate(requestFocus: Boolean): Unit = namedElement.navigate(requestFocus)

      override def canNavigate: Boolean = namedElement.canNavigate

      override def canNavigateToSource: Boolean = namedElement.canNavigateToSource
    }
  }
}
