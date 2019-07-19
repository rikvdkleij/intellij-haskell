package intellij.haskell

sealed abstract class HTool extends Product with Serializable {
  def name: String
}

object HTool {

  case object Hlint extends HTool {
    def name: String = "hlint"
  }

  case object Hindent extends HTool {
    def name: String = "hindent"
  }

  case object Hoogle extends HTool {
    def name: String = "hoogle"
  }

  case object StylishHaskell extends HTool {
    def name: String = "stylish-haskell"
  }
}
