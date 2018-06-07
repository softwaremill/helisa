package com.softwaremill.helisa.api.convert

import com.softwaremill.helisa._
import org.scalacheck.Gen
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.OptionValues._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, MustMatchers}

class DecoderSpec extends FlatSpec with MustMatchers with GeneratorDrivenPropertyChecks {

  it must "decode case classes from a compatible, uniform \"flat\" genotype" in {
    val g = Gen.delay(Genotype.uniform(chromosomes.int(0, 5), chromosomes.int(5, 10)))

    case class SimpleIntParams(a: Int, b: Int)

    forAll(g)(genotype => {
      val tested = genotype.decode[SimpleIntParams]
      tested.value.a must be(genotype.get(0).getGene.getAllele)
      tested.value.b must be(genotype.get(1).getGene.getAllele)
    })

  }

  it must "decode case classes from a compatible, non-uniform \"flat\" genotype" in {
    val g = Gen.delay(Genotype.generic(chromosomes.int(0, 5), chromosomes.double(-0.5, 0.5)))

    case class MixedParams(a: Int, b: Double)

    forAll(g)(genotype => {
      val tested = genotype.decode[MixedParams]
      tested.value.a must be(genotype.get(0).getGene.getAllele)
      tested.value.b must be(genotype.get(1).getGene.getAllele)
    })
  }

  it must "decode case classes from a compatible, uniform \"non-flat\" genotype" in {
    val g = Gen.delay(Genotype.uniform(chromosomes.int(0, 5, 2)))

    case class SimpleIntParams(a: Int, b: Int)

    forAll(g)(genotype => {
      val tested = genotype.decode[SimpleIntParams]
      tested.value.a must be(genotype.get(0).getGene(0).getAllele)
      tested.value.b must be(genotype.get(0).getGene(1).getAllele)
    })
  }

  it must "NOT decode case classes from a non-compatible genotype (arity)" in {
    val g = Gen.delay(Genotype.uniform(chromosomes.int(0, 5), chromosomes.int(5, 10)))

    case class SimpleIntParams(a: Int)

    forAll(g)(genotype => {
      genotype.decode[SimpleIntParams] mustBe empty
    })
  }

  it must "NOT decode case classes from a non-compatible genotype (types)" in {
    val g = Gen.delay(Genotype.uniform(chromosomes.int(0, 5), chromosomes.int(5, 10)))

    case class SimpleIntParams(a: Double, b: Double)

    forAll(g)(genotype => {
      genotype.decode[SimpleIntParams] mustBe empty
    })
  }

}
