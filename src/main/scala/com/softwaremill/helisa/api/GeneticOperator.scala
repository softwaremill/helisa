package com.softwaremill.helisa.api

import io.{jenetics => j}

import scala.compat.java8.FunctionConverters._
import com.softwaremill.helisa._
import io.jenetics.NumericGene

object GeneticOperator {

  private val defaultOperatorProbability: Double = j.Alterer.DEFAULT_ALTER_PROBABILITY

  object crossover {

    def intermediate[G <: NumericGene[_, G], FRC <: Comparable[FRC]](pRecombine: Double = defaultOperatorProbability,
                                                                     cLoc: Int = 0) =
      new j.IntermediateCrossover[G, FRC](pRecombine, cLoc)
    def line[G <: NumericGene[_, G], FRC <: Comparable[FRC]](pRecombine: Double = defaultOperatorProbability, cLoc: Int = 0) =
      new j.LineCrossover[G, FRC](pRecombine, cLoc)

    def multipoint[G <: Gene[_, G], FRC <: Comparable[FRC]](pRecombine: Double = 0.05, numCrossoverPoints: Int = 2) =
      new j.MultiPointCrossover[G, FRC](pRecombine, numCrossoverPoints)

    def partiallyMatched[G <: NumericGene[_, G], FRC <: Comparable[FRC]](pRecombine: Double) =
      new j.PartiallyMatchedCrossover[G, FRC](pRecombine)
    def singlePoint[G <: Gene[_, G], FRC <: Comparable[FRC]](pRecombine: Double = 0.05) =
      new j.SinglePointCrossover[G, FRC](pRecombine)

    def uniform[G <: Gene[_, G], FRC <: Comparable[FRC]](pRecombine: Double = defaultOperatorProbability,
                                                         pSwap: Double = defaultOperatorProbability) =
      new j.UniformCrossover[G, FRC](pRecombine, pSwap)

    def meanOperator[G <: NumericGene[_, G] with j.util.Mean[G], FRC <: Comparable[FRC]](pRecombine: Double = 0.05) =
      new j.MeanAlterer[G, FRC](pRecombine)

    def simulatedBinary[G <: NumericGene[_, G], FRC <: Comparable[FRC]](pRecombine: Double, contiguity: Double = 2.5) =
      new j.ext.SimulatedBinaryCrossover[G, FRC](pRecombine, contiguity)

  }

  object mutator {

    def gaussian[G <: NumericGene[_, G], FRC <: Comparable[FRC]](pMutate: Double = defaultOperatorProbability) =
      new j.GaussianMutator[G, FRC](pMutate)

    def mutator[G <: Gene[_, G], FRC <: Comparable[FRC]](pMutate: Double = 0.01) = new j.Mutator[G, FRC](pMutate)
    def swap[G <: Gene[_, G], FRC <: Comparable[FRC]](pMutate: Double = defaultOperatorProbability) =
      new j.SwapMutator[G, FRC](pMutate)
  }

  object gp {

    def treeMutator[A, G <: TreeGene[A, G], FRC <: Comparable[FRC]](
        fMut: j.ext.util.TreeNode[A] => Unit): j.ext.TreeMutator[A, G, FRC] = (tree: j.ext.util.TreeNode[A]) => fMut(tree)

    def singleNodeCrossover[G <: TreeGene[_, G], FRC <: Comparable[FRC]](pRecombine: Double = defaultOperatorProbability) =
      new j.ext.SingleNodeCrossover[G, FRC]()

  }

}
