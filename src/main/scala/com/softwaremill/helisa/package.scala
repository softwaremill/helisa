package com.softwaremill

import java.lang
import java.util.function

import com.softwaremill.helisa.api.convert.Decoder
import io.jenetics.util
import io.jenetics.util.MSeq
import io.{jenetics => j}

import scala.collection.AbstractSeq
import scala.collection.JavaConverters._

package object helisa {

  type Optimize = j.Optimize

  type Gene[A, G <: Gene[A, G]] = j.Gene[A, G]

  type Genotype[G <: Gene[_, G]] = j.Genotype[G]

  type Phenotype[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] = j.Phenotype[G, FitnessResult]

  type Selector[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] =
    (Seq[Phenotype[G, FitnessResult]], Int, Optimize) => collection.immutable.Seq[Phenotype[G, FitnessResult]]

  implicit class GenotypeDecoder[G <: Gene[_, G]](val geno: Genotype[G]) extends AnyVal {

    def decode[A](implicit decoder: Decoder[A, G]): Option[A] = decoder.decode(geno)

  }

  implicit class PhenotypeDecoder[G <: Gene[_, G]](val pheno: Phenotype[G, _ <: Comparable[_]]) extends AnyVal {

    def decode[A](implicit decoder: Decoder[A, G]): Option[A] = pheno.getGenotype.decode

  }

  implicit class JSelector[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](sel: Selector[G, FitnessResult]) {

    def asJava: j.Selector[G, FitnessResult] = (population, count, opt) => sel(population.asScala, count, opt).asJenetics

  }

  case class AltererResult[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](
      population: collection.immutable.Seq[Phenotype[G, FitnessResult]],
      alterations: Int) {
    def asJenetics = j.AltererResult.of(population.asJenetics, alterations)
  }

  type Alterer[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] =
    (Seq[Phenotype[G, FitnessResult]], Long) => AltererResult[G, FitnessResult]

  implicit class JAlterer[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](alt: Alterer[G, FitnessResult]) {

    def asJava: j.Alterer[G, FitnessResult] = (population, generation) => alt(population.asScala, generation).asJenetics

  }

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

    def map[B](mapper: function.Function[_ >: T, _ <: B]): util.Seq[B] =
      proxied
        .map(mapper.asScala)
        .map(_.asInstanceOf[B])
        .asJenetics //TODO: investigate whether this is a Scala/Java inference quirk

    def append(values: lang.Iterable[_ <: T]): util.Seq[T] = (proxied ++ values.asScala).asJenetics

    def prepend(values: lang.Iterable[_ <: T]): util.Seq[T] = (values.asScala ++ proxied).toSeq.asJenetics

    def subSeq(start: Int): util.Seq[T] = proxied.drop(start - 1).asJenetics

    def subSeq(start: Int, end: Int): util.Seq[T] = proxied.slice(start, end).asJenetics
  }

  class SeqJeneticsIView[T](private val proxied: collection.immutable.Seq[T]) extends j.util.ISeq[T] {
    import scala.compat.java8.FunctionConverters._

    def get(index: Int): T = proxied(index)

    def length(): Int = proxied.length

    def map[B](mapper: function.Function[_ >: T, _ <: B]): util.ISeq[B] =
      proxied
        .map(mapper.asScala)
        .map(_.asInstanceOf[B])
        .asJenetics //TODO: investigate whether this is a Scala/Java inference quirk

    def append(values: lang.Iterable[_ <: T]): util.ISeq[T] = (proxied ++ values.asScala).asJenetics

    def prepend(values: lang.Iterable[_ <: T]): util.ISeq[T] = (values.asScala ++ proxied).to[collection.immutable.Seq].asJenetics

    def subSeq(start: Int): util.ISeq[T] = proxied.drop(start - 1).asJenetics

    def subSeq(start: Int, end: Int): util.ISeq[T] = proxied.slice(start, end).asJenetics

    def copy(): MSeq[T] = MSeq.of(proxied: _*)
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

}
