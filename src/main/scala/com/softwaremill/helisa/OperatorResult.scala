package com.softwaremill.helisa

import io.{jenetics => j}
import com.softwaremill.helisa.internal.InternalImplicits._

/**
  *
  * @param population the resulting population
  * @param alterations the amount of performed alterations
  */
case class OperatorResult[G <: Gene[_, G], FitnessResult <: Comparable[FitnessResult]](
    population: collection.immutable.Seq[Phenotype[G, FitnessResult]],
    alterations: Int) {
  private[helisa] def asJenetics = j.AltererResult.of(population.asJenetics, alterations)
}
