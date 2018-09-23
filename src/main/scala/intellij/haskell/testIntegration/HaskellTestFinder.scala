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

package intellij.haskell.testIntegration

import java.util

import com.intellij.psi.PsiElement
import com.intellij.psi.search.{FilenameIndex, GlobalSearchScope}
import com.intellij.testIntegration.TestFinder

import scala.collection.JavaConverters

/**
  * Triggered when the user uses the "Navigation / Test"  action (= jump to test shortcut)
  * TODO Make "Spec" suffix configurable?
  */
class HaskellTestFinder extends TestFinder {
  override def findSourceElement(psiElement: PsiElement): PsiElement = {
    //TODO Check the use of this function
    psiElement
  }

  override def findTestsForClass(psiElement: PsiElement): util.Collection[PsiElement] = {
    val testFileName = psiElement.getContainingFile.getName.replace(".hs", "Spec.hs")
    val testFiles = FilenameIndex.getFilesByName(psiElement.getProject, testFileName, GlobalSearchScope.allScope(psiElement.getProject))
    JavaConverters.asJavaCollection(testFiles)
  }

  override def findClassesForTest(psiElement: PsiElement): util.Collection[PsiElement] = {
    val sourceFileName = psiElement.getContainingFile.getName.replace("Spec.hs", ".hs")
    val sourceFiles = FilenameIndex.getFilesByName(psiElement.getProject, sourceFileName, GlobalSearchScope.allScope(psiElement.getProject))
    JavaConverters.asJavaCollection(sourceFiles)
  }

  override def isTest(psiElement: PsiElement): Boolean = {
    psiElement.getContainingFile.getName.endsWith("Spec.hs")
  }
}
