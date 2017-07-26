/*
 * Copyright 2014-2017 Rik van der Kleij
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
import intellij.haskell.util.StringUtil
import intellij.haskell.util.index.HaskellModuleNameIndex

class HoogleByNameContributor extends ChooseByNameContributor {

  private final val DeclarationPattern = """([\w\.\-]+) (.*)""".r
  private final val ModulePattern = """module ([\w\.\-]+)""".r
  private final val PackagePattern = """package (.*)""".r

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    Iterable("a").toArray
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    def NotFoundResult(moduleName: String, declaration: String): Iterable[NotFoundNavigationItem] = {
      Iterable(NotFoundNavigationItem(declaration, Some(moduleName)))
    }

    val hooglePattern =
      if (includeNonProjectItems) {
        pattern
      } else {
        HaskellComponentsManager.findProjectPackageNames(project).map(_.foldLeft("")((s: String, pn: String) => s + s"+$pn")).map(_ + s" $pattern").getOrElse(pattern)
      }

    val navigationItems = HoogleComponent.runHoogle(project, hooglePattern, count = 100000).getOrElse(Seq()) flatMap {
      case ModulePattern(moduleName) =>
        ProgressManager.checkCanceled()
        HaskellModuleNameIndex.findHaskellFilesByModuleNameInAllScope(project, moduleName)
      case PackagePattern(packageName) =>
        ProgressManager.checkCanceled()
        Iterable(NotFoundNavigationItem(packageName))
      case DeclarationPattern(moduleName, declaration) =>
        ProgressManager.checkCanceled()
        DeclarationLineUtil.findName(declaration).map(nd => {
          val name = StringUtil.removeOuterParens(nd.name)
          val namedElementsByNameInfo = HaskellComponentsManager.findNameInfoByModuleAndName(project, moduleName, name).flatMap {
            case lni: LibraryNameInfo => HaskellReference.findNamedElementsByLibraryNameInfo(lni, name, project, None, preferExpressions = false)
            case pni: ProjectNameInfo => HaskellReference.findNamedElementByLocation(pni.filePath, pni.lineNr, pni.columnNr, name, project).toIterable
            case _ => Iterable()
          }
          if (namedElementsByNameInfo.isEmpty) {
            val namedElements = HaskellReference.findNamedElementsByModuleNameAndName(moduleName, name, project, None, preferExpressions = false)
            if (namedElements.isEmpty) {
              NotFoundResult(moduleName, declaration)
            } else {
              namedElements.flatMap(HaskellPsiUtil.findDeclarationElementParent)
            }
          } else {
            namedElementsByNameInfo.flatMap(HaskellPsiUtil.findDeclarationElementParent)
          }
        }).getOrElse(NotFoundResult(moduleName, declaration))
      case d =>
        ProgressManager.checkCanceled()
        Iterable(NotFoundNavigationItem(d))
    }
    var i = 0
    navigationItems.map(e => new NavigationItem {

      // Hack to display items in same order as given by Hoogle
      override def getName: String = {
        i = i + 1
        f"$i%06d" + " " + e.getName
      }

      override def getPresentation: ItemPresentation = e.getPresentation

      override def navigate(b: Boolean): Unit = e.navigate(b)

      override def canNavigate: Boolean = canNavigate

      override def canNavigateToSource: Boolean = canNavigateToSource
    }).toArray
  }

  case class NotFoundNavigationItem(declaration: String, moduleName: Option[String] = None) extends NavigationItem {
    override def getName: String = declaration

    override def getPresentation: ItemPresentation = new ItemPresentation {
      override def getIcon(unused: Boolean) = null

      override def getLocationString: String = moduleName.getOrElse("")

      override def getPresentableText: String = getName
    }

    override def canNavigateToSource: Boolean = false

    override def canNavigate: Boolean = false

    override def navigate(requestFocus: Boolean): Unit = ()
  }

}
