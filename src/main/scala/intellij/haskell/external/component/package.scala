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

  case object NoInfoAvailable extends NoInfo {
    override def message: String = "No info available for this request"
  }

  case object IndexNotReady extends NoInfo {
    override def message: String = "No info because index is not ready"
  }
}
