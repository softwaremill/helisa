package com.softwaremill.helisa.api.convert

import com.softwaremill.helisa.Gene
import io.{jenetics => j}
import shapeless.ops.hlist.ToTraversable
import shapeless.ops.traversable.FromTraversable
import shapeless.{Generic, HList}

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._

@implicitNotFound(msg = """Decoder for ${A} , ${G} not found.

For automatic generation, you need these things:
1. *All* fields of the case class you use *must* be compatible with possible Gene values.
2. import com.softwaremill.helisa._
""")
trait Decoder[A, G <: Gene[_, G]] {

  def decode(genotype: j.Genotype[G]): Option[A]

}

object Decoder {

  implicit def caseClassDecoder[A, G <: Gene[_, G], Repr <: HList](implicit g: Generic.Aux[A, Repr],
                                                                   tT: ToTraversable.Aux[Repr, Vector, _],
                                                                   fT: FromTraversable[Repr]): Decoder[A, G] =
    (genotype: j.Genotype[G]) => {
      import shapeless.syntax.std.traversable._
      val repr = genotype.iterator().asScala.map(_.getGene.getAllele).toTraversable.toHList[Repr]
      repr.map(g.from)
    }

}
