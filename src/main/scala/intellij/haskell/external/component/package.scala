package intellij.haskell.external

package object component {

  sealed trait NoInfo {
    def message: String
  }

  case object ReplNotAvailable extends NoInfo {
    def message: String = "No info because REPL is not (yet) available"
  }

  case object ReplIsBusy extends NoInfo {
    def message: String = "No info because REPL is busy at this moment"
  }

  case class NoInfoAvailable(name: String, locationName: String) extends NoInfo {
    override def message: String = s"No info available for $name in $locationName"
  }

  case object IndexNotReady extends NoInfo {
    override def message: String = "No info because index is not ready"
  }

  case class ModuleNotLoaded(fileName: String) extends NoInfo {
    override def message: String = s"No info because module of file $fileName is not loaded"
  }

  case class ReadActionTimeout(readActionDescription: String) extends NoInfo {
    def message = s"No info because read action timeout while $readActionDescription"
  }

}
