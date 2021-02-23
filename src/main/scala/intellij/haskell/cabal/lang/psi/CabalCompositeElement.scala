package intellij.haskell.cabal.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.SearchScope
import com.intellij.psi.{PsiElement, PsiNameIdentifierOwner}
import intellij.haskell.cabal.lang.psi.impl._

sealed trait CabalCompositeElement extends PsiElement

trait CabalNamedElement extends CabalCompositeElement with PsiNameIdentifierOwner with NavigationItem {
  def getUseScope: SearchScope
}

sealed abstract class CabalCompositeElementImpl(node: ASTNode)
  extends ASTWrapperPsiElement(node)
    with CabalCompositeElement {

  override def toString: String = getNode.getElementType.toString
}

sealed abstract class CabalFieldElement(node: ASTNode) extends CabalCompositeElementImpl(node)

sealed abstract class CabalFieldValueElement(node: ASTNode) extends CabalCompositeElementImpl(node)
sealed abstract class CabalStanzaElement(node: ASTNode) extends CabalCompositeElementImpl(node)

final class Freeform(node: ASTNode) extends CabalFieldValueElement(node)
final class IdentList(node: ASTNode) extends CabalFieldValueElement(node)
final class ModuleList(node: ASTNode) extends CabalFieldValueElement(node)
final class Module(node: ASTNode) extends CabalCompositeElementImpl(node) with ModuleImpl
final class ModulePart(node: ASTNode) extends CabalCompositeElementImpl(node) with ModulePartImpl
final class BoolValue(node: ASTNode) extends CabalFieldValueElement(node)

final class FuncCall(node: ASTNode) extends CabalCompositeElementImpl(node)
final class FuncName(node: ASTNode) extends CabalCompositeElementImpl(node)
final class FuncArg(node: ASTNode) extends CabalCompositeElementImpl(node)

final class IfExpr(node: ASTNode) extends CabalCompositeElementImpl(node)
final class IfCond(node: ASTNode) extends CabalCompositeElementImpl(node)
final class ThenBody(node: ASTNode) extends CabalCompositeElementImpl(node)
final class ElseBody(node: ASTNode) extends CabalCompositeElementImpl(node)
final class LogicalNeg(node: ASTNode) extends CabalCompositeElementImpl(node)

final class InvalidField(node: ASTNode) extends CabalFieldElement(node)
final class UnknownField(node: ASTNode) extends CabalFieldElement(node)
final class CustomField(node: ASTNode) extends CabalFieldElement(node)
final class PkgName(node: ASTNode) extends CabalFieldElement(node)
final class PkgVersion(node: ASTNode) extends CabalFieldElement(node)
final class CabalVersion(node: ASTNode) extends CabalFieldElement(node)
final class BuildType(node: ASTNode) extends CabalFieldElement(node)
final class License(node: ASTNode) extends CabalFieldElement(node)
final class LicenseFile(node: ASTNode) extends CabalFieldElement(node)
final class LicenseFiles(node: ASTNode) extends CabalFieldElement(node)
final class Copyright(node: ASTNode) extends CabalFieldElement(node)
final class Author(node: ASTNode) extends CabalFieldElement(node)
final class Maintainer(node: ASTNode) extends CabalFieldElement(node)
final class Stability(node: ASTNode) extends CabalFieldElement(node)
final class Homepage(node: ASTNode) extends CabalFieldElement(node)
final class BugReports(node: ASTNode) extends CabalFieldElement(node)
final class PackageUrl(node: ASTNode) extends CabalFieldElement(node)
final class Synopsis(node: ASTNode) extends CabalFieldElement(node)
final class Description(node: ASTNode) extends CabalFieldElement(node)
final class Category(node: ASTNode) extends CabalFieldElement(node)
final class TestedWith(node: ASTNode) extends CabalFieldElement(node)
final class DataFiles(node: ASTNode) extends CabalFieldElement(node)
final class DataDir(node: ASTNode) extends CabalFieldElement(node)
final class ExtraSourceFiles(node: ASTNode) extends CabalFieldElement(node)
final class ExtraDocFiles(node: ASTNode) extends CabalFieldElement(node)
final class ExtraTmpFiles(node: ASTNode) extends CabalFieldElement(node)
final class MainIs(node: ASTNode) extends CabalFieldElement(node) with MainIsImpl
final class BuildDepends(node: ASTNode) extends CabalFieldElement(node) with BuildDependsImpl
final class Dependencies(node: ASTNode) extends CabalFieldValueElement(node)
final class Dependency(node: ASTNode) extends CabalCompositeElementImpl(node)
final class DependencyVersion(node: ASTNode) extends CabalCompositeElementImpl(node)
final class ThinRenameModules(node: ASTNode) extends CabalCompositeElementImpl(node)
final class WithRenameModules(node: ASTNode) extends CabalCompositeElementImpl(node)
final class RenameModule(node: ASTNode) extends CabalCompositeElementImpl(node)
final class OtherModules(node: ASTNode) extends CabalFieldElement(node)
final class DefaultLanguage(node: ASTNode) extends CabalFieldElement(node)
final class OtherLanguages(node: ASTNode) extends CabalFieldElement(node)
final class Extensions(node: ASTNode) extends CabalFieldElement(node) with ExtensionsImpl
final class DefaultExtensions(node: ASTNode) extends CabalFieldElement(node) with ExtensionsImpl
final class OtherExtensions(node: ASTNode) extends CabalFieldElement(node) with ExtensionsImpl
final class HsSourceDir(node: ASTNode) extends CabalFieldElement(node) with SourceDirsImpl
final class HsSourceDirs(node: ASTNode) extends CabalFieldElement(node) with SourceDirsImpl
final class BuildTools(node: ASTNode) extends CabalFieldElement(node)
final class Buildable(node: ASTNode) extends CabalFieldElement(node)
final class GhcOptions(node: ASTNode) extends CabalFieldElement(node) with GhcOptionsImpl
final class GhcProfOptions(node: ASTNode) extends CabalFieldElement(node) with GhcOptionsImpl
final class GhcSharedOptions(node: ASTNode) extends CabalFieldElement(node) with GhcOptionsImpl
final class Includes(node: ASTNode) extends CabalFieldElement(node)
final class InstallIncludes(node: ASTNode) extends CabalFieldElement(node)
final class IncludeDirs(node: ASTNode) extends CabalFieldElement(node)
final class CSources(node: ASTNode) extends CabalFieldElement(node)
final class JsSources(node: ASTNode) extends CabalFieldElement(node)
final class ExtraLibraries(node: ASTNode) extends CabalFieldElement(node)
final class ExtraGhciLibraries(node: ASTNode) extends CabalFieldElement(node)
final class ExtraLibDirs(node: ASTNode) extends CabalFieldElement(node)
final class CcOptions(node: ASTNode) extends CabalFieldElement(node)
final class CppOptions(node: ASTNode) extends CabalFieldElement(node)
final class LdOptions(node: ASTNode) extends CabalFieldElement(node)
final class PkgconfigDepends(node: ASTNode) extends CabalFieldElement(node)
final class Frameworks(node: ASTNode) extends CabalFieldElement(node)
final class RequiredSignatures(node: ASTNode) extends CabalFieldElement(node)

final class SourceRepo(node: ASTNode) extends CabalStanzaElement(node)
final class SourceRepoType(node: ASTNode) extends CabalFieldElement(node)
final class SourceRepoLocation(node: ASTNode) extends CabalFieldElement(node)
final class SourceRepoModule(node: ASTNode) extends CabalFieldElement(node)
final class SourceRepoBranch(node: ASTNode) extends CabalFieldElement(node)
final class SourceRepoTag(node: ASTNode) extends CabalFieldElement(node)
final class SourceRepoSubdir(node: ASTNode) extends CabalFieldElement(node)

final class FlagDecl(node: ASTNode) extends CabalStanzaElement(node)
final class FlagDescr(node: ASTNode) extends CabalFieldElement(node)
final class FlagDefault(node: ASTNode) extends CabalFieldElement(node)
final class FlagManual(node: ASTNode) extends CabalFieldElement(node)

final class Library(node: ASTNode) extends CabalStanzaElement(node)
final class ExposedModules(node: ASTNode) extends CabalFieldElement(node) with ExposedModulesImpl
final class Exposed(node: ASTNode) extends CabalFieldElement(node)
final class ReexportedModules(node: ASTNode) extends CabalFieldElement(node)
final class ModuleReexport(node: ASTNode) extends CabalFieldValueElement(node)

final class Executable(node: ASTNode) extends CabalStanzaElement(node)

final class TestSuite(node: ASTNode) extends CabalStanzaElement(node)
final class TestSuiteType(node: ASTNode) extends CabalFieldElement(node)

final class Benchmark(node: ASTNode) extends CabalStanzaElement(node)
final class BenchmarkType(node: ASTNode) extends CabalFieldElement(node)

final class InvalidStanza(node: ASTNode) extends CabalStanzaElement(node)
