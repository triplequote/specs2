package org.specs2.fp

/**
 * Inspired from the scalaz (https://github.com/scalaz/scalaz) project
 */
trait Functor[F[_]]:

  def map[A, B](fa: F[A])(f: A => B): F[B]

  def xmap[A, B](fa: F[A], f: A => B, g: B => A): F[B] =
    map(fa)(f)

  def void[A](fa: F[A]): F[Unit] = as(fa)(())

  def as[A, B](fa: F[A])(b: =>B): F[B] = map(fa)(_ => b)

object Functor:
  @inline def apply[F[_]](implicit F: Functor[F]): Functor[F] = F

  implicit val OptionFunctor: Functor[Option[*]] = new Functor[Option[*]] {
    def map[A, B](fa: Option[A])(f: A => B): Option[B] =
      fa.map(f)
  }
  implicit def EitherFunctor[E]: Functor[Either[E, *]] = new Functor[Either[E, *]] {
    def map[A, B](fa: Either[E, A])(f: A => B): Either[E, B] =
      fa.map(f)
  }

trait FunctorSyntax:
  implicit class FunctorOps[F[_] : Functor, A](fa: F[A]):
    def map[B](f: A => B): F[B] =
      Functor.apply[F].map(fa)(f)

    def as[B](b: => B): F[B] =
      Functor.apply[F].as(fa)(b)

    def void: F[Unit] =
      Functor.apply[F].void(fa)

object FunctorSyntax extends FunctorSyntax
