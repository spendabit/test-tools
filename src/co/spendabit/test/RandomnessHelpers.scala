package co.spendabit.test

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

trait RandomnessHelpers {

  protected def randomInt(max: Int): Int =
    scala.util.Random.nextInt(max) + 1

  protected def randomInt(min: Int, max: Int): Int = {
    require(max > min)
    scala.util.Random.nextInt(max - min + 1) + min
  }

  protected def pickOne[T](collection: T*): T =
    randomElem(collection)

  protected def randomElem[T](collection: Seq[T]): T =
    if (collection.size < 1)
      throw new IllegalArgumentException("Given `collection` has no elements")
    else
      shuffle(collection).head

  protected def randomElems[T](collection: Seq[T], count: Int): Seq[T] =
    if (collection.size < count)
      throw new IllegalArgumentException(s"Given `collection` only has $count elements")
    else
      shuffle(collection).take(count)

  protected def shuffle[T, CC[X] <: TraversableOnce[X]](xs: CC[T])
                                                       (implicit bf: CanBuildFrom[CC[T], T, CC[T]]): CC[T] =
    scala.util.Random.shuffle(xs)
}
