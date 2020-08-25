package org.specs2
package data

/**
 * A trait for anything that can be sized
 */
trait Sized[T]:
  /** @return the size of t */
  def size(t: T) : Int

  /** alias for size */
  def length(t: T) : Int = size(t)

object Sized extends SizedLowPriority1:
    /** any scala collection has a size */
  implicit def scalaTraversableIsSized[I <: Traversable[_]]: Sized[I] = new Sized[I] {
    def size(t: I) = t.size
  }

trait SizedLowPriority1 extends SizedLowPriority2:
  /** any java collection has a size */
  implicit def javaCollectionIsSized[T <: java.util.Collection[_]]: Sized[T] = new Sized[T] {
    def size(t: T) = t.size()
  }

  /** any scala array has a size */
  implicit def scalaArrayIsSized[T]: Sized[Array[T]] = new Sized[Array[T]] {
    def size(t: Array[T]) = t.length
  }

trait SizedLowPriority2:

  /** a regular string has a size, without having to be converted to an Traversable */
  implicit def stringIsSized: Sized[String] = new Sized[String] {
    def size(t: String) = t.length
  }

