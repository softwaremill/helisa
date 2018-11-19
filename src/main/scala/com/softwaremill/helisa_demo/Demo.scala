package com.softwaremill.helisa_demo

import com.softwaremill.helisa._

object Demo extends App {

  val Number = 42

  val genotype =
    () => genotypes.uniform(chromosomes.int(0, 100))
  def fitness(toGuess: Int) =
    (guess: Guess) => 1.0 / (guess.num - toGuess).abs

  val evolver =
    Evolver(fitness(Number), genotype)
      .populationSize(100)
      .phenotypeValidator(_.num % 2 == 0)
      .build()

  val stream = evolver.streamScalaStdLib()

  val best = stream.drop(1000).head.bestPhenotype

  println(best)

}

case class Guess(num: Int)
