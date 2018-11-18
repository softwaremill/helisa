package com.softwaremill.helisa_demo

import com.softwaremill.helisa._

object Demo extends App {

  val fitnessFunction = (cannyParams: CannyParameters) =>
    ((cannyParams.low + cannyParams.high + cannyParams.actualBlur) % 8.0) + 1.0

  val evolver =
    Evolver(fitnessFunction, () => genotypes.uniform(chromosomes.int(0, 255), chromosomes.int(0, 255), chromosomes.int(0, 6)))
      .populationSize(100)
      .phenotypeValidator(canny => canny.low <= canny.high)
      .build()

  val stream = evolver.streamScalaStdLib()

  val best = stream.drop(1000).head.bestPhenotype

  println(best)

}

case class CannyParameters(low: Int, high: Int, rawBlur: Int) {

  def actualBlur = rawBlur * 2 - 1

}
