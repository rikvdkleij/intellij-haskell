package intellij.haskell.util

/** NonEmptyList for Sets. */
final case class NonEmptySet[A] private(toSet: Set[A]) extends AnyVal {

  def map[B](f: A => B): NonEmptySet[B] = new NonEmptySet[B](toSet.map(f))

  def foreach[U](f: A => U): Unit = toSet.foreach[U](f)

  def append(ss: NonEmptySet[A]*): NonEmptySet[A] = {
    new NonEmptySet(ss.foldRight(toSet: Set[A])((s, acc) => acc ++ s.toSet))
  }
}

object NonEmptySet {

  def apply[A](a: A, as: A*): NonEmptySet[A] = new NonEmptySet(as.toSet + a)

  def fromSet[A](s: Set[A]): Option[NonEmptySet[A]] = {
    if (s.isEmpty) None else Some(new NonEmptySet(s))
  }

  def fromSets[A](ss: Traversable[Set[A]]): Option[NonEmptySet[A]] = {
    fromSet(ss.foldRight(Set.empty[A])((s, acc) => acc ++ s))
  }

  def fromTraversable[A](s: Traversable[A]): Option[NonEmptySet[A]] = {
    if (s.isEmpty) None else Some(new NonEmptySet(s.toSet))
  }

  def fromTraversables[A](ss: Traversable[Traversable[A]]): Option[NonEmptySet[A]] = {
    fromSets(ss.toStream.map(_.toSet))
  }
}

