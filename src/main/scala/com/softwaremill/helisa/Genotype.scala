package com.softwaremill.helisa

import com.softwaremill.helisa.api.convert.Decoder
import io.{jenetics => j}

object Genotype {

  def apply[A: Decoder[?, G], G <: Gene[_, G]](first: Chromosome[A, G], rest: Chromosome[A, G]*): Genotype[G] =
    j.Genotype.of(first, rest: _*)

}
