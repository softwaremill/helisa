package com.softwaremill.helisa

import scala.annotation.implicitNotFound

@implicitNotFound("""Comparable to {FitnessResult} not found. The result of your fitness function should either:
1. Be a primitive (Int/Long/Double/Float).
2. Implement java.lang.Comparable.""")
trait FitnessResultComparable[FitnessResult] {
  type FitnessResultC <: Comparable[FitnessResultC]
  def apply(c: FitnessResult): FitnessResultC
}

object FitnessResultComparable {
  implicit def fitnessResultComparable[_FitnessResultC <: Comparable[_FitnessResultC]]: FitnessResultComparable[_FitnessResultC] =
    new FitnessResultComparable[_FitnessResultC] {
      type FitnessResultC = _FitnessResultC
      def apply(c: _FitnessResultC): _FitnessResultC = c
    }

  implicit object DoubleFitnessResultComparable extends FitnessResultComparable[Double] {
    type FitnessResultC = java.lang.Double
    def apply(c: Double): java.lang.Double = c
  }

  implicit object FloatFitnessResultComparable extends FitnessResultComparable[Float] {
    type FitnessResultC = java.lang.Float
    def apply(c: Float): java.lang.Float = c
  }

  implicit object IntegerFitnessResultComparable extends FitnessResultComparable[Int] {
    type FitnessResultC = java.lang.Integer
    def apply(c: Int): java.lang.Integer = c
  }

  implicit object LongFitnessResultComparable extends FitnessResultComparable[Long] {
    type FitnessResultC = java.lang.Long

    def apply(c: Long): java.lang.Long = c
  }

}
