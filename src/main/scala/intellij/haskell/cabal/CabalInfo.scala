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

package intellij.haskell.cabal

import java.io.{File, IOException}
import java.nio.charset.StandardCharsets

import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.{PsiElement, PsiFileFactory}
import intellij.haskell.HaskellNotificationGroup
import intellij.haskell.cabal.lang.psi
import intellij.haskell.cabal.lang.psi._
import intellij.haskell.cabal.lang.psi.impl.SourceDirsImpl
import intellij.haskell.psi.HaskellPsiUtil

import scala.io.Source

object CabalInfo {

  def create(project: Project, cabalFile: File): Option[CabalInfo] = {
    val source = try {
      Some(Source.fromFile(cabalFile, StandardCharsets.UTF_8.toString).mkString)
    } catch {
      case e: IOException =>
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not read Cabal file ${cabalFile.getName}, error: ${e.getMessage}")
        None
    }

    source.flatMap(src => PsiFileFactory.getInstance(project).createFileFromText(cabalFile.getName, CabalLanguage.Instance, src) match {
      case cabalPsiFile: CabalFile => Some(new CabalInfo(cabalPsiFile, cabalFile.getParentFile.getAbsolutePath))
      case _ =>
        HaskellNotificationGroup.logErrorBalloonEvent(project, s"Could not parse Cabal file ${cabalFile.getName}")
        None
    })
  }
}

class CabalInfo(cabalFile: CabalFile, val modulePath: String) {

  val packageName: String = (for {
    pkgName <- HaskellPsiUtil.getChildOfType(cabalFile, classOf[PkgName])
    ff <- HaskellPsiUtil.getChildOfType(pkgName, classOf[Freeform])
  } yield ff.getText).getOrElse(throw new IllegalStateException(s"Can not find package name in Cabal file ${cabalFile.getName}"))

  lazy val library: Option[LibraryCabalStanza] = {
    cabalFile.getChildren.collectFirst {
      case c: Library => LibraryCabalStanza(c, packageName, modulePath)
    }
  }

  lazy val executables: Iterable[ExecutableCabalStanza] = {
    HaskellPsiUtil.streamChildren(cabalFile, classOf[Executable]).map(c => ExecutableCabalStanza(c, packageName, modulePath))
  }

  lazy val testSuites: Iterable[TestSuiteCabalStanza] = {
    HaskellPsiUtil.streamChildren(cabalFile, classOf[TestSuite]).map(c => TestSuiteCabalStanza(c, packageName, modulePath))
  }

  lazy val benchmarks: Iterable[BenchmarkCabalStanza] = {
    HaskellPsiUtil.streamChildren(cabalFile, classOf[Benchmark]).map(c => BenchmarkCabalStanza(c, packageName, modulePath))
  }

  lazy val cabalStanzas: Iterable[CabalStanza] = {
    library.toArray ++
      cabalFile.getChildren.collect {
        case c: Executable => ExecutableCabalStanza(c, packageName, modulePath)
        case c: TestSuite => TestSuiteCabalStanza(c, packageName, modulePath)
        case c: Benchmark => BenchmarkCabalStanza(c, packageName, modulePath)
      }
  }

  lazy val sourceRoots: Iterable[String] = {
    library.map(_.sourceDirs).getOrElse(Array()) ++ executables.flatMap(_.sourceDirs)
  }

  lazy val testSourceRoots: Iterable[String] = {
    (testSuites ++ benchmarks).flatMap(_.sourceDirs)
  }

  lazy val ghcOptions: Set[String] = {
    HaskellPsiUtil.streamChildren(cabalFile, classOf[psi.impl.GhcOptionsImpl]).flatMap(_.getValue).toSet
  }
}

sealed trait CabalStanza {

  protected val sectionRootElement: PsiElement
  protected val modulePath: String

  val targetName: String

  val nameElementType: Option[IElementType]

  lazy val sourceDirs: Array[String] = {
    HaskellPsiUtil.getChildOfType(sectionRootElement, classOf[SourceDirsImpl]).map(_.getValue).getOrElse(Array()).map(d => modulePath + File.separator + d)
  }

  lazy val name: Option[String] = {
    nameElementType.flatMap(net => HaskellPsiUtil.getChildNodes(sectionRootElement, net).headOption).map(_.getText)
  }
}

case class LibraryCabalStanza(sectionRootElement: PsiElement, packageName: String, modulePath: String) extends CabalStanza {
  val nameElementType: Option[IElementType] = None

  val targetName: String = s"$packageName:lib"
}

case class ExecutableCabalStanza(sectionRootElement: PsiElement, packageName: String, modulePath: String) extends CabalStanza {
  lazy val nameElementType: Option[IElementType] = Some(CabalTypes.EXECUTABLE_NAME)

  lazy val targetName: String = name.map(n => s"$packageName:exe:$n").getOrElse(throw new IllegalStateException(s"Executable should have name in package $packageName"))
}

case class TestSuiteCabalStanza(sectionRootElement: PsiElement, packageName: String, modulePath: String) extends CabalStanza {
  lazy val nameElementType: Option[IElementType] = Some(CabalTypes.TEST_SUITE_NAME)

  lazy val targetName: String = name.map(n => s"$packageName:test:$n").getOrElse(throw new IllegalStateException(s"Test-suite should have name in package $packageName"))
}

case class BenchmarkCabalStanza(sectionRootElement: PsiElement, packageName: String, modulePath: String) extends CabalStanza {
  lazy val nameElementType: Option[IElementType] = Some(CabalTypes.BENCHMARK_NAME)

  lazy val targetName: String = name.map(n => s"$packageName:bench:$n").getOrElse(throw new IllegalStateException(s"Benchmark should have name in package $packageName"))
}
