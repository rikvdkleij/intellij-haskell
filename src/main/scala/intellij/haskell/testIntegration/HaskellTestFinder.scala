package intellij.haskell.testIntegration

import java.util

import com.intellij.psi.PsiElement
import com.intellij.psi.search.{FilenameIndex, GlobalSearchScope}
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testIntegration.TestFinder
import intellij.haskell.HaskellFile

import scala.jdk.CollectionConverters._

/**
  * Triggered when the user uses the "Navigation / Test"  action (= jump to test shortcut)
  */
class HaskellTestFinder extends TestFinder {

  /**
    * Return the parent PsiFile of the PsiElement where the cursor was when the test finder was invoked, to handle some magic stuff, like the name displayed in "Choose Test for {file name}".
    */
  override def findSourceElement(psiElement: PsiElement): PsiElement = {
    PsiTreeUtil.getParentOfType(psiElement, classOf[HaskellFile])
  }

  /**
    * Given a source PSI element, find all test files this element could be a source of.
    */
  override def findTestsForClass(psiElement: PsiElement): util.Collection[PsiElement] = {
    val testFileName = psiElement.getContainingFile.getName.replace(".hs", "Spec.hs")
    val testFiles: Seq[PsiElement] = FilenameIndex.getFilesByName(psiElement.getProject, testFileName, GlobalSearchScope.projectScope(psiElement.getProject)).toIndexedSeq
    testFiles.asJavaCollection
  }

  /**
    * Given a test PSI element, find all source files this element could be a test of.
    */
  override def findClassesForTest(psiElement: PsiElement): util.Collection[PsiElement] = {
    val sourceFileName = psiElement.getContainingFile.getName.replace("Spec.hs", ".hs")
    val sourceFiles: Seq[PsiElement] = FilenameIndex.getFilesByName(psiElement.getProject, sourceFileName, GlobalSearchScope.projectScope(psiElement.getProject)).toIndexedSeq
    sourceFiles.asJavaCollection
  }

  override def isTest(psiElement: PsiElement): Boolean = {
    psiElement.getContainingFile.getName.endsWith("Spec.hs")
  }
}
