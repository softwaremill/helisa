package com.softwaremill.helisa_demo

import com.softwaremill.helisa._
import io.jenetics.{Genotype, IntegerChromosome}

object Demo extends App {

  val fitnessFunction = (cannyParams: CannyParameters) =>
    ((cannyParams.low + cannyParams.high + cannyParams.actualBlur) % 8.0) + 1.0

  val evolver =
    Evolver(fitnessFunction,
            () => Genotype.of(IntegerChromosome.of(0, 255), IntegerChromosome.of(0, 255), IntegerChromosome.of(0, 6)))
      .populationSize(100)
      .phenotypeValidator(canny => canny.low <= canny.high)
      .build()

  val stream = evolver.streamScalaStdlib()

  val best = stream.drop(1000).head.bestPhenotype

  println(best)

}

case class CannyParameters(low: Int, high: Int, rawBlur: Int) {

  def actualBlur = rawBlur * 2 - 1

}
