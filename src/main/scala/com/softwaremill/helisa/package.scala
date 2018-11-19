package com.softwaremill

import java.lang
import java.util.function

import com.softwaremill.helisa.api.{Chromosome, GeneticOperator, InternalImplicits, Selector}
import com.softwaremill.helisa.api.convert.Decoder
import io.{jenetics => j}

import scala.collection.AbstractSeq
import scala.collection.JavaConverters._

package object helisa {
  import InternalImplicits._

  type Optimize = j.Optimize

  object Optimize {
    val Maximum = j.Optimize.MAXIMUM
    val Minimum = j.Optimize.MINIMUM
  }

  type Gene[A, G <: Gene[A, G]]                                              = j.Gene[A, G]
  type NumericGene[A <: Number with Comparable[Any], G <: NumericGene[A, G]] = j.NumericGene[A, G]
  type TreeGene[A, G <: TreeGene[A, G]]                                      = j.ext.TreeGene[A, G]

  type Genotype[G <: Gene[_, G]] = j.Genotype[G]

  type Phenotype[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] = j.Phenotype[G, FitnessResult]

  type Selector[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] =
    (Seq[Phenotype[G, FitnessResult]], Int, Optimize) => collection.immutable.Seq[Phenotype[G, FitnessResult]]

  type GeneticOperator[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] =
    (Seq[Phenotype[G, FitnessResult]], Long) => OperatorResult[G, FitnessResult]

  type Chromosome[A, G <: Gene[_, G]] = j.Chromosome[G]

  val operators = GeneticOperator

  val selectors = Selector.standard

  val chromosomes = Chromosome

  val genotypes = Genotype

  implicit class GenotypeDecoder[G <: Gene[_, G]](val geno: Genotype[G]) extends AnyVal {

    def decode[A](implicit decoder: Decoder[A, G]): Option[A] = decoder.decode(geno)

  }

  implicit class PhenotypeDecoder[G <: Gene[_, G]](val pheno: Phenotype[G, _ <: Comparable[_]]) extends AnyVal {

    def decode[A](implicit decoder: Decoder[A, G]): Option[A] = pheno.getGenotype.decode

  }

  implicit class JSelector[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](val sel: Selector[G, FitnessResult])
      extends AnyVal {

    def asJenetics: j.Selector[G, FitnessResult] = (population, count, opt) => sel(population.asScala, count, opt).asJenetics

  }

  case class OperatorResult[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](
      population: collection.immutable.Seq[Phenotype[G, FitnessResult]],
      alterations: Int) {
    def asJenetics = j.AltererResult.of(population.asJenetics, alterations)
  }

  implicit class JGeneticOperator[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](
      val alt: GeneticOperator[G, FitnessResult])
      extends AnyVal {

    def asJenetics: j.Alterer[G, FitnessResult] = (population, generation) => alt(population.asScala, generation).asJenetics

  }

}
