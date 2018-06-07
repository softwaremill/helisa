package com.softwaremill.helisa;


/**
 * Necessary to enforce the required raw types.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class GenotypeHelper {

    static io.jenetics.Genotype wildcardGenotype(io.jenetics.Chromosome first, io.jenetics.Chromosome... rest) {
        return io.jenetics.Genotype.of(first, rest);
    }
}
