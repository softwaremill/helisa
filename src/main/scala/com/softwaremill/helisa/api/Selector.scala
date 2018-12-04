package com.softwaremill.helisa.api

import io.{jenetics => j}

import scala.compat.java8.FunctionConverters._
import com.softwaremill.helisa._

object Selector {

  object standard {

    def elite[G <: Gene[_, G], Fitness <: Comparable[Fitness]](eliteCount: Int) = new j.EliteSelector[G, Fitness](eliteCount)

    def boltzman[G <: Gene[_, G], Fitness <: Number with Comparable[Fitness]](b: Double = 0.4) =
      new j.BoltzmannSelector[G, Fitness](b)

    def exponentialRank[G <: Gene[_, G], Fitness <: Comparable[Fitness]](c: Double = 0.975) =
      new j.ExponentialRankSelector[G, Fitness](c)

    def linearRank[G <: Gene[_, G], Fitness <: Comparable[Fitness]](nminus: Double = 0.5) =
      new j.LinearRankSelector[G, Fitness](nminus)

    def monteCarlo[G <: Gene[_, G], Fitness <: Comparable[Fitness]]() = new j.MonteCarloSelector[G, Fitness]()

    def rouletteWheel[G <: Gene[_, G], Fitness <: Number with Comparable[Fitness]]() = new j.RouletteWheelSelector[G, Fitness]()

    def stochasticUniversal[G <: Gene[_, G], Fitness <: Number with Comparable[Fitness]]() =
      new j.StochasticUniversalSelector[G, Fitness]()

    def tournament[G <: Gene[_, G], Fitness <: Comparable[Fitness]](sampleSize: Int = 2) =
      new j.TournamentSelector[G, Fitness](sampleSize)

    def truncation[G <: Gene[_, G], Fitness <: Comparable[Fitness]](n: Int = Int.MaxValue) =
      new j.TruncationSelector[G, Fitness](n)

  }

}
