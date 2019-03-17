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
import com.intellij.openapi.project.Project
import intellij.haskell.external.component.NameInfoComponentResult.{LibraryNameInfo, ProjectNameInfo}
import intellij.haskell.external.component._
import intellij.haskell.psi.{HaskellDeclarationElement, HaskellPsiUtil}
import intellij.haskell.util.index.HaskellModuleNameIndex
import intellij.haskell.util.{HaskellFileUtil, StringUtil}
import javax.swing.Icon

class HoogleByNameContributor extends ChooseByNameContributor {

  private final val DeclarationPattern = """([\w\.\-]+) (.*)""".r
  private final val ModulePattern = """module ([\w\.\-]+)""".r
  private final val PackagePattern = """package (.*)""".r

  override def getNames(project: Project, includeNonProjectItems: Boolean): Array[String] = {
    Iterable("a").toArray
  }

  override def getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array[NavigationItem] = {
    def NotFoundResult(moduleName: String, declaration: String): Seq[NotFoundNavigationItem] = {
      Seq(NotFoundNavigationItem(declaration, Some(moduleName)))
    }

    val hooglePattern =
      if (includeNonProjectItems) {
        pattern
      } else {
        HaskellComponentsManager.findProjectPackageNames(project).map(_.foldLeft("")((s: String, pn: String) => s + s"+$pn")).map(_ + s" $pattern").getOrElse(pattern)
      }

    ProgressManager.checkCanceled()

    val navigationItems = HoogleComponent.runHoogle(project, hooglePattern, count = 25).getOrElse(Seq()).flatMap {
      case ModulePattern(moduleName) =>
        ProgressManager.checkCanceled()
        HaskellModuleNameIndex.findFilesByModuleName(project, moduleName) match {
          case Right(files) => files
          case _ => Seq()
        }
      case PackagePattern(packageName) =>
        ProgressManager.checkCanceled()
        Seq(NotFoundNavigationItem(packageName))
      case DeclarationPattern(moduleName, declaration) =>
        ProgressManager.checkCanceled()
        DeclarationLineUtil.findName(declaration).toSeq.flatMap(nd => {
          val name = StringUtil.removeOuterParens(nd.name)
          ProgressManager.checkCanceled()
          val result = HaskellComponentsManager.findNameInfoByModuleName(project, moduleName, name)
          ProgressManager.checkCanceled()
          val navigationItemByNameInfo = result.toOption.flatMap(_.headOption) match {
            case Some(lni: LibraryNameInfo) => HaskellReference.findIdentifiersByLibraryNameInfo(project, lni, name).toOption.
              flatMap(x => HaskellPsiUtil.findDeclarationElement(x._2)).map(d => createLibraryNavigationItem(d, moduleName)).toSeq
            case Some(pni: ProjectNameInfo) =>
              HaskellFileUtil.findFileInRead(project, pni.filePath) match {
                case (Some(virtualFile), Right(psiFile)) => HaskellReference.findIdentifierByLocation(project, virtualFile, psiFile, pni.lineNr, pni.columnNr, name).flatMap(HaskellPsiUtil.findDeclarationElement).toSeq
                case (_, _) => Seq()
              }
            case _ => Seq()
          }
          ProgressManager.checkCanceled()
          if (navigationItemByNameInfo.nonEmpty) {
            navigationItemByNameInfo
          } else {
            val identifiers = HaskellReference.findIdentifiersByModuleAndName(project, moduleName, name).toOption.map(_._2).toSeq
            if (identifiers.isEmpty) {
              NotFoundResult(moduleName, declaration)
            } else {
              identifiers.flatMap(e => HaskellPsiUtil.findDeclarationElement(e))
            }
          }
        })
      case d =>
        ProgressManager.checkCanceled()
        Seq(NotFoundNavigationItem(d))
    }

    navigationItems.groupBy(_.getPresentation.getLocationString).flatMap(_._2.headOption).zipWithIndex.map({ case (item, i) => new NavigationItem {

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
