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

package intellij.haskell.navigation

import com.intellij.navigation.{ChooseByNameContributor, ItemPresentation, NavigationItem}
import com.intellij.openapi.project.Project
import intellij.haskell.external.component._
import intellij.haskell.util.HaskellProjectUtil

class HoogleByNameContributor extends ChooseByNameContributor {

  private final val DeclarationPattern = """([\w\.\-]+) (.*)""".r
  private final val ModulePattern = """module ([\w\.\-]+)""".r
  private final val PackagePattern = """package (.*)""".r

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    Iterable("a").toArray
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    def NotFoundResult(moduleName: String, declaration: String) = {
      Iterable(NotFoundNavigationItem(declaration, Some(moduleName)))
    }

    val hooglePattern =
      if (includeNonProjectItems) {
        pattern
      } else {
        HaskellProjectUtil.findCabalPackageName(project).map(pn => s"+$pn $pattern").getOrElse(pattern)
      }

    val navigationItems = HoogleComponent.runHoogle(project, hooglePattern, count = 100000).getOrElse(Seq()) flatMap {
      case ModulePattern(moduleName) => HaskellComponentsManager.findHaskellFiles(project, moduleName)
      case PackagePattern(packageName) => Iterable(NotFoundNavigationItem(packageName))
      case DeclarationPattern(moduleName, declaration) =>
        DeclarationLineUtil.findName(declaration).map(nd => {
          val namedElementsByNameInfo = HaskellComponentsManager.findNameInfoByModuleAndName(project, moduleName, nd.name).flatMap {
            case lni: LibraryNameInfo => HaskellReference.findNamedElementsByLibraryNameInfo(lni, nd.name, project)
            case pni: ProjectNameInfo => HaskellReference.findReferenceByLocation(pni.filePath, pni.lineNr, pni.columnNr, nd.name, project).toIterable
            case _ => Iterable()
          }
          if (namedElementsByNameInfo.isEmpty) {
            val namedElements = HaskellReference.findNamedElementsInModule(moduleName, nd.name, project)
            if (namedElements.isEmpty) {
              NotFoundResult(moduleName, declaration)
            } else {
              namedElements
            }
          } else {
            namedElementsByNameInfo
          }
        }).getOrElse(NotFoundResult(moduleName, declaration))
      case d => Iterable(NotFoundNavigationItem(d))
    }
    navigationItems.toArray
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
