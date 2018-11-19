package com.softwaremill.helisa.api

import io.{jenetics => j}

import scala.compat.java8.FunctionConverters._
import com.softwaremill.helisa._
import InternalImplicits._

object Chromosome {

  def int(min: Int = Int.MinValue, max: Int = Int.MaxValue, length: Int = 1): j.IntegerChromosome =
    j.IntegerChromosome.of(min, max, length)

  def double(min: Double = Double.MinValue, max: Double = Double.MaxValue, length: Int = 1): j.DoubleChromosome =
    j.DoubleChromosome.of(min, max, length)

  def long(min: Long = Long.MinValue, max: Long = Long.MaxValue, length: Int = 1): j.LongChromosome =
    j.LongChromosome.of(min, max, length)

  def string(alleles: String, validChars: String): j.CharacterChromosome =
    j.CharacterChromosome.of(alleles, j.util.CharSeq.of(validChars))

  def bigInt(min: BigInt, max: BigInt, length: Int = 1): j.ext.BigIntegerChromosome =
    j.ext.BigIntegerChromosome.of(min.bigInteger, max.bigInteger, length)

  def any[A](gen: () => A, validator: A => Boolean = (_: Any) => true, length: Int = 1): j.AnyChromosome[A] =
    j.AnyChromosome.of(gen.asJava, validator.asJava, length)

  def bit(chars: CharSequence, p: Double, length: Int = 1): j.BitChromosome = j.BitChromosome.of(chars, length, p)

  object gp {

    type Op[T] = j.prog.op.Op[T]

    object op {

      def unary[T](name: String, function: T => T): Op[T] = j.prog.op.Op.of(name, function.asJava)

      def binary[T](name: String, function: (T, T) => T): Op[T] = j.prog.op.Op.of(name, function.asJava)

      def generic[T <: AnyRef](name: String, arity: Int, function: Array[T] => T): Op[T] =
        j.prog.op.Op.of(name, arity, function.asJava)

    }

    def create[T](depth: Int,
                  operations: collection.immutable.Seq[Op[T]],
                  terminals: collection.immutable.Seq[Op[T]],
                  validator: j.prog.ProgramChromosome[T] => Boolean = (_: Any) => true): j.prog.ProgramChromosome[T] =
      j.prog.ProgramChromosome.of(depth, validator.asJava, terminals.asJenetics, operations.asJenetics)

  }

}
