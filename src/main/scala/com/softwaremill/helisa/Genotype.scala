package com.softwaremill.helisa

import io.{jenetics => j}
import language.existentials._

object Genotype {

  def uniform[G <: Gene[_, G]](first: Chromosome[_, G], rest: Chromosome[_, G]*): Genotype[G] =
    j.Genotype.of(first, rest: _*)

  type WildcardChromosome = Chromosome[_, G] forSome { type G <: Gene[_, G] }

  def generic(first: WildcardChromosome, rest: WildcardChromosome*): Genotype[_] =
    GenotypeHelper.wildcardGenotype(first, rest: _*)

}
