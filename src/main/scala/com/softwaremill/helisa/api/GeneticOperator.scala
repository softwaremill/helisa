package com.softwaremill.helisa.api

import io.{jenetics => j}

import scala.compat.java8.FunctionConverters._
import com.softwaremill.helisa._
import io.jenetics.NumericGene

object GeneticOperator {

  private val defaultOperatorProbability: Double = j.Alterer.DEFAULT_ALTER_PROBABILITY

  /**
    * Crossover operators, intended to simulate recombination between genotypes in a population.
    */
  object crossover {

    def intermediate[G <: NumericGene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double = defaultOperatorProbability,
                                                                             cLoc: Int = 0) =
      new j.IntermediateCrossover[G, Fitness](pRecombine, cLoc)

    def line[G <: NumericGene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double = defaultOperatorProbability,
                                                                     cLoc: Int = 0) =
      new j.LineCrossover[G, Fitness](pRecombine, cLoc)

    def multipoint[G <: Gene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double = 0.05, numCrossoverPoints: Int = 2) =
      new j.MultiPointCrossover[G, Fitness](pRecombine, numCrossoverPoints)

    def partiallyMatched[G <: NumericGene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double) =
      new j.PartiallyMatchedCrossover[G, Fitness](pRecombine)

    def singlePoint[G <: Gene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double = 0.05) =
      new j.SinglePointCrossover[G, Fitness](pRecombine)

    def uniform[G <: Gene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double = defaultOperatorProbability,
                                                                 pSwap: Double = defaultOperatorProbability) =
      new j.UniformCrossover[G, Fitness](pRecombine, pSwap)

    def meanOperator[G <: NumericGene[_, G] with j.util.Mean[G], Fitness <: Comparable[Fitness]](pRecombine: Double = 0.05) =
      new j.MeanAlterer[G, Fitness](pRecombine)

    def simulatedBinary[G <: NumericGene[_, G], Fitness <: Comparable[Fitness]](pRecombine: Double, contiguity: Double = 2.5) =
      new j.ext.SimulatedBinaryCrossover[G, Fitness](pRecombine, contiguity)

  }

  /**
    * Operators introducing small changes to the genotypes of the population to e.g. prevent elitism
    */
  object mutator {

    def gaussian[G <: NumericGene[_, G], Fitness <: Comparable[Fitness]](pMutate: Double = defaultOperatorProbability) =
      new j.GaussianMutator[G, Fitness](pMutate)

    def mutator[G <: Gene[_, G], Fitness <: Comparable[Fitness]](pMutate: Double = 0.01) = new j.Mutator[G, Fitness](pMutate)
    def swap[G <: Gene[_, G], Fitness <: Comparable[Fitness]](pMutate: Double = defaultOperatorProbability) =
      new j.SwapMutator[G, Fitness](pMutate)
  }

  /**
    * Genetic Programming operators
    */
  object gp {

    def treeMutator[A, G <: TreeGene[A, G], Fitness <: Comparable[Fitness]](
        fMut: j.ext.util.TreeNode[A] => Unit): j.ext.TreeMutator[A, G, Fitness] = (tree: j.ext.util.TreeNode[A]) => fMut(tree)

    def singleNodeCrossover[G <: TreeGene[_, G], Fitness <: Comparable[Fitness]](
        pRecombine: Double = defaultOperatorProbability) =
      new j.ext.SingleNodeCrossover[G, Fitness]()

  }

}
