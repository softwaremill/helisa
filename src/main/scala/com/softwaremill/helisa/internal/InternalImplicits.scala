package com.softwaremill.helisa.internal
import java.lang
import java.util.function

import com.softwaremill.helisa.{Gene, GeneticOperator, Selector}
import io.{jenetics => j}

import scala.collection.JavaConverters._
import scala.collection.AbstractSeq

private[helisa] object InternalImplicits {

  class JeneticsSeqView[T](private val proxied: j.util.Seq[T]) extends AbstractSeq[T] {
    def length: Int = proxied.length()

    def apply(idx: Int): T = proxied.get(idx)

    def iterator: Iterator[T] = proxied.iterator().asScala
  }

  class JeneticsISeqView[T](private val proxied: j.util.ISeq[T]) extends collection.immutable.Seq[T] {
    def length: Int = proxied.length()

    def apply(idx: Int): T = proxied.get(idx)

    def iterator: Iterator[T] = proxied.iterator().asScala
  }

  class SeqJeneticsView[T](private val proxied: Seq[T]) extends j.util.Seq[T] {
    import scala.compat.java8.FunctionConverters._

    def get(index: Int): T = proxied(index)

    def length(): Int = proxied.length

    def map[B](mapper: function.Function[_ >: T, _ <: B]): j.util.Seq[B] =
      proxied
        .map(mapper.asScala)
        .map(_.asInstanceOf[B])
        .asJenetics //TODO: investigate whether this is a Scala/Java inference quirk

    def append(values: lang.Iterable[_ <: T]): j.util.Seq[T] = (proxied ++ values.asScala).asJenetics

    def prepend(values: lang.Iterable[_ <: T]): j.util.Seq[T] = (values.asScala ++ proxied).toSeq.asJenetics

    def subSeq(start: Int): j.util.Seq[T] = proxied.drop(start - 1).asJenetics

    def subSeq(start: Int, end: Int): j.util.Seq[T] = proxied.slice(start, end).asJenetics
  }

  class SeqJeneticsIView[T](private val proxied: collection.immutable.Seq[T]) extends j.util.ISeq[T] {
    import scala.compat.java8.FunctionConverters._

    def get(index: Int): T = proxied(index)

    def length(): Int = proxied.length

    def map[B](mapper: function.Function[_ >: T, _ <: B]): j.util.ISeq[B] =
      proxied
        .map(mapper.asScala)
        .map(_.asInstanceOf[B])
        .asJenetics //TODO: investigate whether this is a Scala/Java inference quirk

    def append(values: lang.Iterable[_ <: T]): j.util.ISeq[T] = (proxied ++ values.asScala).asJenetics

    def prepend(values: lang.Iterable[_ <: T]): j.util.ISeq[T] =
      (values.asScala ++ proxied).to[collection.immutable.Seq].asJenetics

    def subSeq(start: Int): j.util.ISeq[T] = proxied.drop(start - 1).asJenetics

    def subSeq(start: Int, end: Int): j.util.ISeq[T] = proxied.slice(start, end).asJenetics

    def copy(): j.util.MSeq[T] = j.util.MSeq.of(proxied: _*)
  }

  implicit class ScalaJeneticsSeq[T](val sSeq: Seq[T]) extends AnyVal {

    def asJenetics: j.util.Seq[T] = new SeqJeneticsView[T](sSeq)

  }

  implicit class JeneticsToScalaSeq[T](val jSeq: j.util.Seq[T]) extends AnyVal {

    def asScala: Seq[T] = new JeneticsSeqView[T](jSeq)

  }

  implicit class ScalaJeneticsISeq[T](val sSeq: collection.immutable.Seq[T]) extends AnyVal {

    def asJenetics: j.util.ISeq[T] = new SeqJeneticsIView[T](sSeq)

  }

  implicit class JeneticsIToScalaISeq[T](val jSeq: j.util.ISeq[T]) extends AnyVal {

    def asScala: collection.immutable.Seq[T] = new JeneticsISeqView[T](jSeq)

  }

  implicit class JSelector[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](val sel: Selector[G, FitnessResult])
      extends AnyVal {

    def asJenetics: j.Selector[G, FitnessResult] = (population, count, opt) => sel(population.asScala, count, opt).asJenetics

  }

  implicit class JGeneticOperator[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](
      val alt: GeneticOperator[G, FitnessResult])
      extends AnyVal {

    def asJenetics: j.Alterer[G, FitnessResult] = (population, generation) => alt(population.asScala, generation).asJenetics

  }

}
