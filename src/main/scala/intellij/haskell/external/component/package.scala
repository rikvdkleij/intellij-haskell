package intellij.haskell.external

package object component {

  sealed trait NoInfo {
    def message: String
  }

  case object ReplNotAvailable extends NoInfo {
    def message: String = "No info because REPL isn't (yet) available"
  }

  case class NoInfoAvailable(name: String, locationName: String) extends NoInfo {
    override def message: String = s"No info available for $name in $locationName"
  }

  case object IndexNotReady extends NoInfo {
    override def message: String = "No info because index isn't ready"
  }

  case class ModuleNotAvailable(name: String) extends NoInfo {
    override def message: String = s"No info because $name isn't loaded or found"
  }

  case class ReadActionTimeout(readActionDescription: String) extends NoInfo {
    def message = s"No info because read action timed out while $readActionDescription"
  }

  // GHCi output: No matching export in any local modules.
  case object NoMatchingExport extends NoInfo {
    def message: String = "No matching export in any local modules"
  }
}
