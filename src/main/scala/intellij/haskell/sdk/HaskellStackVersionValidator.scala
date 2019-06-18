package intellij.haskell.sdk

import com.intellij.util.text.VersionComparatorUtil

object HaskellStackVersionValidator {

  final val MinimumVersion = "1.7.0"

  def validate(maybeVersion: Option[String]): Unit = {
    validate(maybeVersion, MinimumVersion)
  }

  private[sdk] def validate(version: Option[String], minimumVersion: String): Unit = {
    if (!isValid(version, minimumVersion)) {
      throw new Exception(s"Stack version should be > $minimumVersion")
    }
  }

  private[sdk] def isValid(version: Option[String], minimumVersion: String): Boolean = {
    version.map(_.trim) match {
      case Some(v) if VersionComparatorUtil.compare(v, minimumVersion) >= 0 => true
      case _ => false
    }
  }
}
