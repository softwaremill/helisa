package com.softwaremill.helisa

import akka.NotUsed
import akka.stream.scaladsl.{Keep, Sink, Source, StreamConverters}
import cats.effect.Async
import com.softwaremill.helisa.api.convert.{CodecBuilder, Decoder}
import io.{jenetics => j}
import org.reactivestreams.Publisher
import fs2.{Stream => Fs2Stream}

import scala.collection.JavaConverters._
import com.softwaremill.helisa.internal.InternalImplicits._

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import Optimize.Optimize
import com.softwaremill.helisa.api.{EvolutionResult, FitnessResultComparable}

object Evolver {

  /**
    * Creates a new [[EvolverBuilder]] to parametrize the new evolutionary run
    * @see EvolverBuilder.build
    */
  def apply[A, G <: Gene[_, G], @specialized(Int, Double, Float, Long) FitnessResult](fitnessFunction: A => FitnessResult,
                                                                                      genotype: () => Genotype[G])(
      implicit decoder: Decoder[A, G],
      evComp: FitnessResultComparable[FitnessResult]): EvolverBuilder[A, G, evComp.FitnessResultC] = {

    import scala.compat.java8.FunctionConverters._

    val codec: j.engine.Codec[A, G] = CodecBuilder.codecFor[A](genotype)
    new EvolverBuilder(j.engine.Engine.builder(fitnessFunction.andThen(evComp(_)).asJava, codec))
  }

}

/**
  * Representation of an evolutionary run, provides different methods for iteration/streaming
  * @see Evolver.apply
  */
class Evolver[A: Decoder[?, G], G <: Gene[_, G], Fitness <: Comparable[Fitness]](val jEngine: j.engine.Engine[G, Fitness]) {

  def iterator(): Iterator[EvolutionResult[A, G, Fitness]] =
    jEngine.stream().iterator().asScala.map(new EvolutionResult(_))

  def streamScalaStdLib(): Stream[EvolutionResult[A, G, Fitness]] =
    iterator().toStream

  def source(): Source[EvolutionResult[A, G, Fitness], NotUsed] =
    StreamConverters.fromJavaStream(() => jEngine.stream()).map(new EvolutionResult(_))

  val akkaStreamSource: Source[EvolutionResult[A, G, Fitness], NotUsed] = source

  def fs2[F[_]: Async](): Fs2Stream[F, EvolutionResult[A, G, Fitness]] =
    Fs2Stream.fromIterator(iterator())

  def publisher(): Publisher[EvolutionResult[A, G, Fitness]] = {
    import akka.actor.ActorSystem
    import akka.stream.ActorMaterializer

    implicit val ec: ExecutionContext  = ExecutionContext.fromExecutor(jEngine.getExecutor)
    implicit val as: ActorSystem       = ActorSystem(s"helisa_${System.currentTimeMillis()}")
    implicit val am: ActorMaterializer = ActorMaterializer()

    val (watch, publisher) = source().watchTermination()(Keep.right).toMat(Sink.asPublisher(true))(Keep.both).run()

    watch.onComplete { _ =>
      am.shutdown()
      as.terminate()
    }

    publisher
  }

}

/**
  * Allows to configure the [[Evolver]] for the evolutionary run
  * @see EvolverBuilder.build
  */
class EvolverBuilder[A: Decoder[?, G], G <: Gene[_, G], Fitness <: Comparable[Fitness]] private[helisa] (
    private[helisa] val jBuilder: j.engine.Engine.Builder[G, Fitness]) {
  import scala.compat.java8.FunctionConverters._

  /**
    * Completes the Evolver configuration
    * @return a new [[Evolver]] with the appropriate parameters
    * @see Evolver
    */
  def build(): Evolver[A, G, Fitness] = new Evolver(jBuilder.build())

  def populationSize(size: Int) = modifyBuilder(_.populationSize(size))

  def phenotypeValidator(validator: A => Boolean) =
    modifyBuilder(_.phenotypeValidator(_.getGenotype.decode[A].exists(validator)))

  def geneticOperatorsAsFunctions(operator1: GeneticOperator[G, Fitness], rest: GeneticOperator[G, Fitness]*) =
    //Different name for type inference to work correctly
    modifyBuilder(_.alterers(operator1.asJenetics, rest.map(_.asJenetics): _*))

  def geneticOperators(operator1: j.Alterer[G, Fitness], rest: j.Alterer[G, Fitness]*) =
    modifyBuilder(_.alterers(operator1, rest: _*))

  def offspringPopulationSize(size: Int) = modifyBuilder(_.offspringSize(size))

  def executor(ec: ExecutionContext) = modifyBuilder(_.executor(ec.execute(_)))

  def maximalPhenotypeAge(age: Long) = modifyBuilder(_.maximalPhenotypeAge(age))

  def selectorAsFunction(selector: Selector[G, Fitness]) = modifyBuilder(_.selector(selector.asJenetics))

  def selector(selector: j.Selector[G, Fitness]) = modifyBuilder(_.selector(selector))

  def offspringSelectorAsFunction(selector: Selector[G, Fitness]) = modifyBuilder(_.offspringSelector(selector.asJenetics))

  def offspringSelector(selector: j.Selector[G, Fitness]) = modifyBuilder(_.offspringSelector(selector))

  def survivorsSelectorAsFunction(selector: Selector[G, Fitness]) = modifyBuilder(_.survivorsSelector(selector.asJenetics))

  def survivorsSelector(selector: j.Selector[G, Fitness]) = modifyBuilder(_.survivorsSelector(selector))

  def fitnessScaler(scaler: Fitness => Fitness) = modifyBuilder(_.fitnessScaler(scaler.asJava))

  def genotypeValidator(validator: Genotype[G] => Boolean) = modifyBuilder(_.genotypeValidator(validator.asJava))

  def optimize(optimize: Optimize) = modifyBuilder(_.optimize(optimize))

  def maximizing() = modifyBuilder(_.maximizing())

  def minimizing() = modifyBuilder(_.minimizing())

  def offspringFraction(fraction: Double) = modifyBuilder(_.offspringFraction(fraction))

  def survivorsFraction(fraction: Double) = modifyBuilder(_.survivorsFraction(fraction))

  def survivorsSize(size: Int) = modifyBuilder(_.survivorsSize(size))

  def clock(clock: java.time.Clock) = modifyBuilder(_.clock(clock))

  def individualCreationRetries(retries: Int) = modifyBuilder(_.individualCreationRetries(retries))

  private def modifyBuilder(mod: j.engine.Engine.Builder[G, Fitness] => Unit): EvolverBuilder[A, G, Fitness] = {
    mod(jBuilder)
    this
  }

}
