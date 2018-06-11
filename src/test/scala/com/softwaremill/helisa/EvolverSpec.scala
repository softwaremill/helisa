package com.softwaremill.helisa

import java.time.{Clock, Instant, ZoneId}

import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1}
import org.scalatest.{FlatSpec, Inside, MustMatchers}
import io.{jenetics => j}

import scala.concurrent.ExecutionContext

class EvolverSpec extends FlatSpec with MustMatchers with Inside with TableDrivenPropertyChecks {

  case class Blah(a: Int)

  type TB        = EvolverBuilder[Blah, j.IntegerGene, java.lang.Double]
  type IntGene   = j.IntegerGene
  type JDouble   = java.lang.Double
  type TAlterer  = j.Alterer[IntGene, JDouble]
  type TSelector = j.Selector[IntGene, JDouble]

  val StandardOperators: TableFor1[TAlterer] = {
    import operators.crossover._
    Table(
      "operator",
      intermediate(),
      line(),
      multipoint(),
      singlePoint(),
      uniform(),
      meanOperator(),
      simulatedBinary(0.01)
    )
  }

  val StandardSelectors: TableFor1[TSelector] = {
    import selectors._
    Table(
      "selector",
      elite(2),
      boltzman(),
      exponentialRank(),
      linearRank(),
      monteCarlo(),
      rouletteWheel(),
      stochasticUniversal(),
      tournament(),
      truncation()
    )
  }

  "The builder" must "set the phenotype validator" in {
    settingMustWorkWithoutError { tested =>
      val validator: Blah => Boolean = _ => true

      //smoke test, since field inaccessible in original builder
      tested.phenotypeValidator(validator)
    }
  }
  it must "set Java API genetic operators" in {
    forAll(StandardOperators) { operator =>
      ((b: TB) => b.geneticOperators(_: TAlterer)) must preserveValueOf(operator)(_.jBuilder.getAlterers)
    }
  }
  it must "set Scala API genetic operators" in {
    settingMustWorkWithoutError(_.geneticOperatorsAsFunctions((phenotypes, _) => OperatorResult(List(phenotypes: _*), 0)))
  }

  it must "set the population size" in {
    ((b: TB) => b.populationSize _) must preserveValueOf(10)(_.jBuilder.getPopulationSize)
  }

  it must "set the offspring population size" in {
    settingMustWorkWithoutError(_.offspringPopulationSize(10))
  }

  it must "set the executor" in {
    settingMustWorkWithoutError(_.executor(ExecutionContext.Implicits.global))
  }
  it must "set the maximum phenotype age" in {
    ((b: TB) => b.maximalPhenotypeAge _) must preserveValueOf(100L)(_.jBuilder.getMaximalPhenotypeAge)
  }

  it must "set Java API selectors" in {
    forAll(StandardSelectors) { selector =>
      ((b: TB) => b.selector(_: TSelector)) must preserveValueOf(selector)(_.jBuilder.getOffspringSelector)
      ((b: TB) => b.selector(_: TSelector)) must preserveValueOf(selector)(_.jBuilder.getSurvivorsSelector)
    }
  }
  it must "set Scala API selectors" in {
    settingMustWorkWithoutError(_.selectorAsFunction((phenotypes, _, _) => List(phenotypes: _*)))
  }

  it must "set Java API offspring selectors" in {
    forAll(StandardSelectors) { selector =>
      ((b: TB) => b.offspringSelector(_: TSelector)) must preserveValueOf(selector)(_.jBuilder.getOffspringSelector)
    }
  }
  it must "set Scala API offspring selectors" in {
    settingMustWorkWithoutError(_.offspringSelectorAsFunction((phenotypes, _, _) => List(phenotypes: _*)))
  }

  it must "set Java API survivor selectors" in {
    forAll(StandardSelectors) { selector =>
      ((b: TB) => b.survivorsSelector(_: TSelector)) must preserveValueOf(selector)(_.jBuilder.getSurvivorsSelector)
    }
  }
  it must "set Scala API survivor selectors" in {
    settingMustWorkWithoutError(_.survivorsSelectorAsFunction((phenotypes, _, _) => List(phenotypes: _*)))
  }

  it must "set the fitness scaler" in {
    settingMustWorkWithoutError(_.fitnessScaler(_ * 2.0))
  }

  it must "set the genotype validator" in {
    settingMustWorkWithoutError(_.genotypeValidator(_ => true))
  }

  it must "set the Optimize variant" in {
    ((b: TB) => b.optimize _) must preserveValueOf(Optimize.Maximum)(_.jBuilder.getOptimize)
    ((b: TB) => b.optimize _) must preserveValueOf(Optimize.Minimum)(_.jBuilder.getOptimize)
  }

  it must "set to maximizing" in {
    settingMustWorkWithoutError(_.maximizing())
  }

  it must "set to minimizng" in {
    settingMustWorkWithoutError(_.minimizing())
  }

  it must "set the offspring fraction" in {
    ((b: TB) => b.offspringFraction _) must preserveValueOf(0.3)(_.jBuilder.getOffspringFraction)
  }
  it must "set the survivor fraction" in {
    settingMustWorkWithoutError(_.survivorsFraction(0.4))
  }

  it must "set the clock" in {
    ((b: TB) => b.clock _) must preserveValueOf(Clock.fixed(Instant.now(), ZoneId.systemDefault()))(_.jBuilder.getClock)
  }

  it must "set the number of individual creation retries" in {
    ((b: TB) => b.individualCreationRetries _) must preserveValueOf(13)(_.jBuilder.getIndividualCreationRetries)
  }

  def tested = Evolver((_: Blah) => 0.0, () => Genotype.uniform(chromosomes.int(0, 5)))

  def preserveValueOf[A](value: A)(getter: TB => A) = be(value) compose { (setter: TB => A => TB) =>
    getter(setter(tested)(value))
  }

  def settingMustWorkWithoutError(f: TB => Unit): Unit =
    noException mustBe thrownBy {
      f(tested)
    }

}
