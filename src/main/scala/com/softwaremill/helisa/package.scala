package com.softwaremill

import com.softwaremill.helisa.api.{Chromosome, GeneticOperator, Selector}
import com.softwaremill.helisa.api.convert.Decoder
import io.jenetics.Optimize
import io.{jenetics => j}

/**
  * Collection of necessary types and values needed to construct an evolutionary algorithm run with an
  * [[com.softwaremill.helisa.Evolver]].
  *
  * Import this package first with:
  *
  * `import com.softwaremill.helisa._`
  *
  * then create a new `Evolver` with [[com.softwaremill.helisa.Evolver.apply]].
  * @see Evolver
  */
package object helisa {

  /**
    * A single gene from a specific Genotype, usually mapping a singular parameter in the problem space
    * @tparam A the value type, e.g. [[String]] or [[Int]]
    * @tparam G the gene self type (required by jenetics)
    */
  type Gene[A, G <: Gene[A, G]]                                              = j.Gene[A, G]
  type NumericGene[A <: Number with Comparable[Any], G <: NumericGene[A, G]] = j.NumericGene[A, G]
  type TreeGene[A, G <: TreeGene[A, G]]                                      = j.ext.TreeGene[A, G]

  /**
    * A collection of Chromosomes for a given problem, representing all the parameters of the problem space
    * in a form that the evolutionary algorythm can process
    * @tparam G the gene self type (required by jenetics)
    */
  type Genotype[G <: Gene[_, G]] = j.Genotype[G]

  /**
    * A collection of values representing a solution in the given problem space
    * @tparam G self type (required by jenetics)
    * @tparam FitnessResult the type of the value returned by a fitness function, e.g. [[Int]] or [[Double]]
    */
  type Phenotype[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] = j.Phenotype[G, FitnessResult]

  /**
    * A function that influences what genotypes will pass to the next generation in an
    * evolutionary run
    * @tparam G self type (required by jenetics)
    * @tparam FitnessResult the type of the value returned by a fitness function, e.g. [[Int]] or [[Double]]
    */
  type Selector[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] =
    (Seq[Phenotype[G, FitnessResult]], Int, Optimize) => collection.immutable.Seq[Phenotype[G, FitnessResult]]

  /**
    * A function modifying the population of the next generation based on the current one, e.g. recombination or mutation
    * @tparam G self type (required by jenetics)
    * @tparam FitnessResult the type of the value returned by a fitness function, e.g. [[Int]] or [[Double]]
    */
  type GeneticOperator[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]] =
    (Seq[Phenotype[G, FitnessResult]], Long) => OperatorResult[G, FitnessResult]

  /**
    * All standard genetic operators
    * @see GeneticOperator
    */
  val operators = GeneticOperator

  /**
    * All standard selectors
    * @see Selector
    */
  val selectors = Selector.standard

  /**
    * Chromosome builders
    * @see Chromosome
    */
  val chromosomes = Chromosome

  /**
    * Genotype builders
    * @see Genotype
    */
  val genotypes = Genotype

  /**
    * Provides conversion from a Genotype to a compatible case class.
    */
  implicit class GenotypeDecoder[G <: Gene[_, G]](val geno: Genotype[G]) extends AnyVal {

    def decode[A](implicit decoder: Decoder[A, G]): Option[A] = decoder.decode(geno)

  }

  /**
    * Provides conversion from a Genotype to a compatible case class that represents it.
    */
  implicit class PhenotypeDecoder[G <: Gene[_, G]](val pheno: Phenotype[G, _ <: Comparable[_]]) extends AnyVal {

    def decode[A](implicit decoder: Decoder[A, G]): Option[A] = pheno.getGenotype.decode

  }

}
