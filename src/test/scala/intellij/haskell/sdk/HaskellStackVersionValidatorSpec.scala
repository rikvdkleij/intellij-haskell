package intellij.haskell.sdk

import org.scalatest._

class HaskellStackVersionValidatorSpec extends FlatSpec {

  import intellij.haskell.sdk.HaskellStackVersionValidator.validate

  final val Major = 3
  final val Minor = 4
  final val Patch = 5
  final val MinimumVersion = s"$Major.$Minor.$Patch"

  def buildVersion(major: Int, minor: Int, patch: Int): Option[String] = {
    Some(s"$major.$minor.$patch\n")
  }

  "A higher major version" should "pass" in {
    validate(buildVersion(Major + 1, Minor, Patch), MinimumVersion)
  }

  "A higher minor version" should "pass" in {
    validate(buildVersion(Major, Minor + 1, Patch), MinimumVersion)
  }

  "A higher patch version" should "pass" in {
    validate(buildVersion(Major, Minor, Patch + 1), MinimumVersion)
  }

  "Exactly the minimum version" should "pass" in {
    validate(buildVersion(Major, Minor, Patch), MinimumVersion)
  }

  "An empty version" should "throw an Exception" in {
    assertThrows[Exception] {
      validate(None)
    }
  }

  "An empty string version" should "throw an Exception" in {
    assertThrows[Exception] {
      validate(Some(""))
    }
  }

  "A lower major version" should "throw an Exception" in {
    assertThrows[Exception] {
      validate(buildVersion(Major - 1, Minor, Patch), MinimumVersion)
    }
  }

  "A lower minor version" should "throw an Exception" in {
    assertThrows[Exception] {
      validate(buildVersion(Major, Minor - 1, Patch), MinimumVersion)
    }
  }

  "A lower patch version" should "throw an Exception" in {
    assertThrows[Exception] {
      validate(buildVersion(Major, Minor, Patch - 1), MinimumVersion)
    }
  }

  "An invalid version format" should "throw an Exception" in {
    assertThrows[Exception] {
      validate(Some("invalid version\n"), MinimumVersion)
    }
  }

}
