package com.softwaremill.helisa.api

import io.{jenetics => j}

import scala.compat.java8.FunctionConverters._
import com.softwaremill.helisa._

object Selector {

  object standard {

    def elite[G <: Gene[_, G], FRC <: Comparable[FRC]](eliteCount: Int) = new j.EliteSelector[G, FRC](eliteCount)

    def boltzman[G <: Gene[_, G], FRC <: Number with Comparable[FRC]](b: Double = 0.4) = new j.BoltzmannSelector[G, FRC](b)

    def exponentialRank[G <: Gene[_, G], FRC <: Comparable[FRC]](c: Double = 0.975) = new j.ExponentialRankSelector[G, FRC](c)

    def linearRank[G <: Gene[_, G], FRC <: Comparable[FRC]](nminus: Double = 0.5) = new j.LinearRankSelector[G, FRC](nminus)

    def monteCarlo[G <: Gene[_, G], FRC <: Comparable[FRC]]() = new j.MonteCarloSelector[G, FRC]()

    def rouletteWheel[G <: Gene[_, G], FRC <: Number with Comparable[FRC]]() = new j.RouletteWheelSelector[G, FRC]()

    def stochasticUniversal[G <: Gene[_, G], FRC <: Number with Comparable[FRC]]() = new j.StochasticUniversalSelector[G, FRC]()

    def tournament[G <: Gene[_, G], FRC <: Comparable[FRC]](sampleSize: Int = 2) = new j.TournamentSelector[G, FRC](sampleSize)

    def truncation[G <: Gene[_, G], FRC <: Comparable[FRC]](n: Int = Int.MaxValue) = new j.TruncationSelector[G, FRC](n)

  }

}
