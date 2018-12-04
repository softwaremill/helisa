package com.softwaremill.helisa.api

import com.softwaremill.helisa._
import com.softwaremill.helisa.api.convert.Decoder
import com.softwaremill.helisa.internal.InternalImplicits._
import io.{jenetics => j}

/**
  * Result of the current evolutionary iteration, provided by an iterator/stream created from an
  * [[Evolver]]
  */
class EvolutionResult[A: Decoder[?, G], G <: Gene[_, G], Fitness <: Comparable[Fitness]](
    private val jResult: j.engine.EvolutionResult[G, Fitness]) {

  def optimize = jResult.getOptimize

  def population = jResult.getPopulation.asScala.flatMap(_.decode[A].toSeq)

  def bestPhenotype: Option[A] = jResult.getBestPhenotype.decode

  def worstPhenotype: Option[A] = jResult.getWorstPhenotype.decode

  def generation: Long = jResult.getGeneration

  def totalGenerations: Long = jResult.getTotalGenerations

  def killCount = jResult.getKillCount

  def operatorCount = jResult.getAlterCount

  def invalidCount = jResult.getInvalidCount

  def bestFitness = jResult.getBestFitness

  def worstFitness = jResult.getWorstFitness

}
