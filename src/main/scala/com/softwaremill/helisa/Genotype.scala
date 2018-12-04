package com.softwaremill.helisa

import io.{jenetics => j}

import language.existentials

object Genotype {

  type Chromosome[A, G <: Gene[_, G]] = j.Chromosome[G]
  type WildcardChromosome             = Chromosome[_, G] forSome { type G <: Gene[_, G] }

  /**
    * Generates genotype where all chromosomes have the same type
    */
  def uniform[G <: Gene[_, G]](first: Chromosome[_, G], rest: Chromosome[_, G]*): Genotype[G] =
    j.Genotype.of(first, rest: _*)

  /**
    * Generates a genotype where chromosomes can have different types
    */
  def generic(first: WildcardChromosome, rest: WildcardChromosome*): Genotype[_] =
    GenotypeHelper.wildcardGenotype(first, rest: _*)

}
