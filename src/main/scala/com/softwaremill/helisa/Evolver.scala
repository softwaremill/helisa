package com.softwaremill.helisa

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source, StreamConverters}
import com.softwaremill.helisa.api.convert.{CodecBuilder, Decoder}
import io.{jenetics => j}
import org.reactivestreams.Publisher

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object Evolver {

  def apply[A, G <: Gene[_, G], @specialized(Int, Double, Float, Long) FitnessResult](fitnessFunction: A => FitnessResult,
                                                                                      genotype: () => Genotype[G])(
      implicit decoder: Decoder[A, G],
      evComp: FitnessResultComparable[FitnessResult]): EvolverBuilder[A, G, evComp.FitnessResultC] = {

    import scala.compat.java8.FunctionConverters._

    val codec: j.engine.Codec[A, G] = CodecBuilder.codecFor[A](genotype)
    new EvolverBuilder(j.engine.Engine.builder(fitnessFunction.andThen(evComp(_)).asJava, codec))
  }

}

class Evolver[A: Decoder[?, G], G <: Gene[_, G], FRC <: Comparable[FRC]](val jEngine: j.engine.Engine[G, FRC]) {

  def streamScalaStdlib(): Stream[EvolutionResult[A, G, FRC]] =
    jEngine.stream().iterator().asScala.map(new EvolutionResult(_)).toStream

  def publisher(): Publisher[EvolutionResult[A, G, FRC]] = {
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

  def source(): Source[EvolutionResult[A, G, FRC], NotUsed] =
    StreamConverters.fromJavaStream(() => jEngine.stream()).map(new EvolutionResult(_))

}

class EvolverBuilder[A: Decoder[?, G], G <: Gene[_, G], FRC <: Comparable[FRC]] private[helisa] (
    private[helisa] val jBuilder: j.engine.Engine.Builder[G, FRC]) {
  import scala.compat.java8.FunctionConverters._

  def phenotypeValidator(validator: A => Boolean) =
    modifyBuilder(_.phenotypeValidator(_.getGenotype.decode[A].exists(validator)))

  def geneticOperatorsAsFunctions(operator1: GeneticOperator[G, FRC], rest: GeneticOperator[G, FRC]*) =
    //Different name for type inference to work correctly
    modifyBuilder(_.alterers(operator1.asJenetics, rest.map(_.asJenetics): _*))

  def geneticOperators(operator1: j.Alterer[G, FRC], rest: j.Alterer[G, FRC]*) = modifyBuilder(_.alterers(operator1, rest: _*))

  def populationSize(size: Int) = modifyBuilder(_.populationSize(size))

  def offspringPopulationSize(size: Int) = modifyBuilder(_.offspringSize(size))

  def executor(ec: ExecutionContext) = modifyBuilder(_.executor(ec.execute(_)))
  def maximalPhenotypeAge(age: Long) = modifyBuilder(_.maximalPhenotypeAge(age))

  def selectorAsFunction(selector: Selector[G, FRC]) = modifyBuilder(_.selector(selector.asJenetics))

  def selector(selector: j.Selector[G, FRC]) = modifyBuilder(_.selector(selector))

  def offspringSelectorAsFunction(selector: Selector[G, FRC]) = modifyBuilder(_.offspringSelector(selector.asJenetics))

  def offspringSelector(selector: j.Selector[G, FRC]) = modifyBuilder(_.offspringSelector(selector))

  def survivorsSelectorAsFunction(selector: Selector[G, FRC]) = modifyBuilder(_.survivorsSelector(selector.asJenetics))

  def survivorsSelector(selector: j.Selector[G, FRC]) = modifyBuilder(_.survivorsSelector(selector))

  def fitnessScaler(scaler: FRC => FRC) = modifyBuilder(_.fitnessScaler(scaler.asJava))

  def genotypeValidator(validator: Genotype[G] => Boolean) = modifyBuilder(_.genotypeValidator(validator.asJava))

  def optimize(optimize: Optimize) = modifyBuilder(_.optimize(optimize))

  def maximizing() = modifyBuilder(_.maximizing())

  def minimizing() = modifyBuilder(_.minimizing())

  def offspringFraction(fraction: Double) = modifyBuilder(_.offspringFraction(fraction))

  def survivorsFraction(fraction: Double) = modifyBuilder(_.survivorsFraction(fraction))

  def survivorsSize(size: Int) = modifyBuilder(_.survivorsSize(size))

  def clock(clock: java.time.Clock) = modifyBuilder(_.clock(clock))

  def individualCreationRetries(retries: Int) = modifyBuilder(_.individualCreationRetries(retries))

  def build(): Evolver[A, G, FRC] = new Evolver(jBuilder.build())

  private def modifyBuilder(mod: j.engine.Engine.Builder[G, FRC] => Unit): EvolverBuilder[A, G, FRC] = {
    mod(jBuilder)
    this
  }

}
