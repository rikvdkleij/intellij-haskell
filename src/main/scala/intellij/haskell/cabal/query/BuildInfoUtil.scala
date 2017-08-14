package intellij.haskell.cabal.query

import java.util

import intellij.haskell.HaskellFile

object BuildInfoUtil {

  def getBuildInfo(haskellFile: HaskellFile): Option[BuildInfo] = for {
    cabalFile <- CabalFileFinder.psiForFile(haskellFile)
    q = new CabalQuery(cabalFile)
    buildInfo <- q.findBuildInfoForSourceFile(haskellFile)
  } yield buildInfo

  def getExtensionOpts(buildInfo: BuildInfo): util.List[String] = {
    val xs = buildInfo.getExtensions
    val result = new util.ArrayList[String](xs.size)
    xs.foreach { x => result.add("-X" + x) }
    result
  }

  def getExtensionOpts(o: Option[BuildInfo]): util.List[String] = o match {
    case None => util.Collections.emptyList()
    case Some(bi) => getExtensionOpts(bi)
  }

  def getGhcOpts(buildInfo: BuildInfo): util.List[String] = {
    val xs = buildInfo.getGhcOptions
    val result = new util.ArrayList[String](xs.size)
    xs.foreach {
      result.add
    }
    result
  }

  def getGhcOpts(o: Option[BuildInfo]): util.List[String] = o match {
    case None => util.Collections.emptyList()
    case Some(bi) => getGhcOpts(bi)
  }
}
