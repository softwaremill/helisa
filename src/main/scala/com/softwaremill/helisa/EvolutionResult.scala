package com.softwaremill.helisa

import com.softwaremill.helisa.api.convert.Decoder
import io.{jenetics => j}

class EvolutionResult[A: Decoder[?, G], G <: Gene[_, G], FRC <: Comparable[FRC]](val jResult: j.engine.EvolutionResult[G, FRC]) {

  def optimize = jResult.getOptimize

  def population = jResult.getPopulation.asScala.flatMap(_.decode[A].toSeq)

  def bestPhenotype: Option[A] = jResult.getBestPhenotype.decode

  def worstPhenotype: Option[A] = jResult.getWorstPhenotype.decode

  def generation: Long = jResult.getGeneration

  def totalGenerations: Long = jResult.getTotalGenerations

  def killCount = jResult.getKillCount

  def altererCount = jResult.getAlterCount

  def invalidCount = jResult.getInvalidCount

  def bestFitness = jResult.getBestFitness

  def worstFitness = jResult.getWorstFitness

}
