/*
 * Copyright 2014-2018 Rik van der Kleij
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
import com.intellij.openapi.project.{DumbService, Project}
import intellij.haskell.external.component.NameInfoComponentResult.{LibraryNameInfo, ProjectNameInfo}
import intellij.haskell.external.component._
import intellij.haskell.psi.{HaskellDeclarationElement, HaskellPsiUtil}
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{ScalaUtil, StringUtil}
import javax.swing.Icon

class HoogleByNameContributor extends ChooseByNameContributor {

  private final val DeclarationPattern = """([\w\.\-]+) (.*)""".r
  private final val ModulePattern = """module ([\w\.\-]+)""".r
  private final val PackagePattern = """package (.*)""".r

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    Iterable("a").toArray
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    def NotFoundResult(moduleName: String, declaration: String): Option[NotFoundNavigationItem] = {
      Some(NotFoundNavigationItem(declaration, Some(moduleName)))
    }

    val hooglePattern =
      if (includeNonProjectItems) {
        pattern
      } else {
        HaskellComponentsManager.findProjectPackageNames(project).map(_.foldLeft("")((s: String, pn: String) => s + s"+$pn")).map(_ + s" $pattern").getOrElse(pattern)
      }

    val navigationItems = HoogleComponent.runHoogle(project, hooglePattern, count = 100000).getOrElse(Seq()).flatMap {
      case ModulePattern(moduleName) =>
        ProgressManager.checkCanceled()
        Option(DumbService.getInstance(project).tryRunReadActionInSmartMode(ScalaUtil.computable(HaskellModuleNameIndex.findHaskellFilesByModuleNameInAllScope(project, moduleName)), "Hoogle not available until indices are ready")).getOrElse(Iterable())
      case PackagePattern(packageName) =>
        ProgressManager.checkCanceled()
        Iterable(NotFoundNavigationItem(packageName))
      case DeclarationPattern(moduleName, declaration) =>
        ProgressManager.checkCanceled()
        DeclarationLineUtil.findName(declaration).map(nd => {
          val name = StringUtil.removeOuterParens(nd.name)
          val result = HaskellComponentsManager.findNameInfoByModuleName(project, moduleName, name)
          val navigationItemByNameInfo = result.toOption.flatMap(_.headOption) match {
            case Some(lni: LibraryNameInfo) => HaskellReference.findIdentifiersByLibraryNameInfo(lni, name, project, None, preferExpressions = false).
              headOption.flatMap(HaskellPsiUtil.findDeclarationElementParent).map(d => createLibraryNavigationItem(d, moduleName))
            case Some(pni: ProjectNameInfo) => HaskellReference.findIdentifierByLocation(project, pni.filePath, pni.lineNr, pni.columnNr, name)._2.flatMap(HaskellPsiUtil.findDeclarationElementParent)
            case _ => None
          }
          navigationItemByNameInfo.orElse {
            val identifier = HaskellReference.findIdentifiersByModuleName(project, None, moduleName, name).headOption
            if (identifier.isEmpty) {
              NotFoundResult(moduleName, declaration)
            } else {
              identifier.flatMap(HaskellPsiUtil.findDeclarationElementParent)
            }
          }
        }).getOrElse(NotFoundResult(moduleName, declaration))
      case d =>
        ProgressManager.checkCanceled()
        Iterable(NotFoundNavigationItem(d))
    }

    var i = 0
    navigationItems.map(item => new NavigationItem {

      // Hack to display items in same order as given by Hoogle
      override def getName: String = {
        i = i + 1
        f"$i%06d" + " " + item.getName
      }

      override def getPresentation: ItemPresentation = item.getPresentation

      override def navigate(b: Boolean): Unit = item.navigate(b)

      override def canNavigate: Boolean = canNavigate

      override def canNavigateToSource: Boolean = canNavigateToSource
    }).toArray
  }

  private def createLibraryNavigationItem(namedElement: HaskellDeclarationElement, moduleNameFromHoogle: String): NavigationItem = {
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

  case class NotFoundNavigationItem(declaration: String, moduleName: Option[String] = None) extends NavigationItem {
    override def getName: String = declaration

    override def getPresentation: ItemPresentation = new ItemPresentation {
      override def getIcon(unused: Boolean): Icon = null

      override def getLocationString: String = moduleName.getOrElse("")

      override def getPresentableText: String = getName
    }

    override def canNavigateToSource: Boolean = false

    override def canNavigate: Boolean = false

    override def navigate(requestFocus: Boolean): Unit = ()
  }

}
