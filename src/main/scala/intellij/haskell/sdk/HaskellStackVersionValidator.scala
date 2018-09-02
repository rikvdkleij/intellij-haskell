package intellij.haskell.sdk

import com.intellij.util.text.VersionComparatorUtil

object HaskellStackVersionValidator {

  final val MinimumVersion = "1.7.0"

  def validate(maybeVersion: Option[String]): Unit = {
    validate(maybeVersion, MinimumVersion)
  }

  private[sdk] def validate(maybeVersion: Option[String], minimumVersion: String): Unit = {
    maybeVersion.map(version => version.trim) match {
      case Some(version) if VersionComparatorUtil.compare(version, minimumVersion) >= 0 => ()
      case _ => throw new Exception(s"Stack version should be > $minimumVersion")
    }
  }

}
