package com.softwaremill.helisa.api.convert

import java.util.function
import io.{jenetics => j}
import com.softwaremill.helisa.Gene

abstract class CodecBuilder[A] {
  def apply[G <: Gene[_, G]](genotype: () => j.Genotype[G])(implicit d: Decoder[A, G]): j.engine.Codec[A, G]
}

object CodecBuilder {
  def codecFor[A] = new CodecBuilder[A] {
    def apply[G <: Gene[_, G]](genotype: () => j.Genotype[G])(implicit d: Decoder[A, G]) = new j.engine.Codec[A, G] {
      def encoding(): j.util.Factory[j.Genotype[G]] = () => genotype()

      def decoder(): function.Function[j.Genotype[G], A] =
        (jGenotype: j.Genotype[G]) =>
          d.decode(jGenotype).getOrElse(throw new IllegalStateException(s"Can't convert this genotype: $jGenotype"))
    }
  }
}
