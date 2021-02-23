package intellij.haskell

sealed abstract class HTool extends Product with Serializable {
  def name: String
}

object HTool {

  case object Hlint extends HTool {
    def name: String = "hlint"
  }

  case object Hoogle extends HTool {
    def name: String = "hoogle"
  }

  case object StylishHaskell extends HTool {
    def name: String = "stylish-haskell"
  }

  case object Ormolu extends HTool {
    def name: String = "ormolu"

  }

}
