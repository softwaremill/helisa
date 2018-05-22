package com.softwaremill.helisa

import com.softwaremill.helisa.api.convert.{CodecBuilder, Decoder}
import io.{jenetics => j}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object Evolver {

  def apply[A, G <: Gene[_, G], @specialized(Int, Double, Float, Long) FitnessResult](fitnessFunction: A => FitnessResult,
                                                                                      genotype: () => Genotype[G])(
      implicit decoder: Decoder[A, G],
      evComp: FitnessResultComparable[FitnessResult]): EvolverBuilder[A, G, evComp.FitnessResultC] = {

    import java.util.function._
    import scala.compat.java8.FunctionConverters._

    val codec: j.engine.Codec[A, G] = CodecBuilder.codecFor[A](genotype)
    new EvolverBuilder(j.engine.Engine.builder(fitnessFunction.andThen(evComp(_)).asJava, codec))
  }

}

class Evolver[A: Decoder[?, G], G <: Gene[_, G], FRC <: Comparable[FRC]](val jEngine: j.engine.Engine[G, FRC]) {

  def stream(): Stream[EvolutionResult[A, G, FRC]] = jEngine.stream().iterator().asScala.map(new EvolutionResult(_)).toStream

}

class EvolverBuilder[A: Decoder[?, G], G <: Gene[_, G], FRC <: Comparable[FRC]] private[helisa] (
    private val jBuilder: j.engine.Engine.Builder[G, FRC]) {
  import scala.compat.java8.FunctionConverters._

  private def modifyBuilder(mod: j.engine.Engine.Builder[G, FRC] => Unit): EvolverBuilder[A, G, FRC] = {
    mod(jBuilder)
    this
  }

  def fitnessScaler(scaler: FRC => FRC) = modifyBuilder(_.fitnessScaler(scaler.asJava))

  def offspringSelector(selector: Selector[G, FRC]) = modifyBuilder(_.offspringSelector(selector.asJava))

  def offspringSelector(selector: j.Selector[G, FRC]) = modifyBuilder(_.offspringSelector(selector))

  def survivorsSelector(selector: Selector[G, FRC]) = modifyBuilder(_.survivorsSelector(selector.asJava))

  def survivorsSelector(selector: j.Selector[G, FRC]) = modifyBuilder(_.survivorsSelector(selector))

  def sSelector(selector: Selector[G, FRC]) = modifyBuilder(_.selector(selector.asJava))

  def selector(selector: j.Selector[G, FRC]) = modifyBuilder(_.selector(selector))

  def alterers(alterer1: Alterer[G, FRC], rest: Alterer[G, FRC]*) =
    modifyBuilder(_.alterers(alterer1.asJava, rest.map(_.asJava): _*))

  def alterers(alterer1: j.Alterer[G, FRC], rest: j.Alterer[G, FRC]*) = modifyBuilder(_.alterers(alterer1, rest: _*))

  def phenotypeValidator(validator: A => Boolean) =
    modifyBuilder(_.phenotypeValidator(_.getGenotype.decode[A].exists(validator)))

  def genotypeValidator(validator: Genotype[G] => Boolean) = modifyBuilder(_.genotypeValidator(validator.asJava))

  def optimize(optimize: Optimize) = modifyBuilder(_.optimize(optimize))

  def maximizing() = modifyBuilder(_.maximizing())

  def minimizing() = modifyBuilder(_.minimizing())

  def offspringFraction(fraction: Double) = modifyBuilder(_.offspringFraction(fraction))

  def survivorsFraction(fraction: Double) = modifyBuilder(_.survivorsFraction(fraction))

  def survivorsSize(size: Int) = modifyBuilder(_.survivorsSize(size))

  def offspringSize(size: Int) = modifyBuilder(_.offspringSize(size))

  def populationSize(size: Int) = modifyBuilder(_.populationSize(size))

  def maximalPhenotypeAge(age: Long) = modifyBuilder(_.maximalPhenotypeAge(age))

  def executor(ec: ExecutionContext) = modifyBuilder(_.executor(ec.execute(_)))

  def clock(clock: java.time.Clock) = modifyBuilder(_.clock(clock))

  def individualCreationRetries(retries: Int) = modifyBuilder(_.individualCreationRetries(retries))

  def build(): Evolver[A, G, FRC] = new Evolver(jBuilder.build())

}
