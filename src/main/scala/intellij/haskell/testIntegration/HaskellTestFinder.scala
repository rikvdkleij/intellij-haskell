package intellij.haskell.testIntegration

import java.util

import com.intellij.psi.PsiElement
import com.intellij.psi.search.{FilenameIndex, GlobalSearchScope}
import com.intellij.testIntegration.TestFinder

import scala.collection.JavaConverters

/**
  * Triggered when the user uses the "Navigation / Test"  action (= jump to test shortcut)
  */
class HaskellTestFinder extends TestFinder {

  override def findSourceElement(psiElement: PsiElement): PsiElement = {
    //TODO Check the use of this function
    psiElement
  }

  /**
    * Given a source PSI element, find all test files this element could be a source of.
    */
  override def findTestsForClass(psiElement: PsiElement): util.Collection[PsiElement] = {
    val testFileName = psiElement.getContainingFile.getName.replace(".hs", "Spec.hs")
    val testFiles = FilenameIndex.getFilesByName(psiElement.getProject, testFileName, GlobalSearchScope.allScope(psiElement.getProject))
    JavaConverters.asJavaCollection(testFiles)
  }

  /**
    * Given a test PSI element, find all source files this element could be a test of.
    */
  override def findClassesForTest(psiElement: PsiElement): util.Collection[PsiElement] = {
    val sourceFileName = psiElement.getContainingFile.getName.replace("Spec.hs", ".hs")
    val sourceFiles = FilenameIndex.getFilesByName(psiElement.getProject, sourceFileName, GlobalSearchScope.allScope(psiElement.getProject))
    JavaConverters.asJavaCollection(sourceFiles)
  }

  override def isTest(psiElement: PsiElement): Boolean = {
    psiElement.getContainingFile.getName.endsWith("Spec.hs")
  }
}
