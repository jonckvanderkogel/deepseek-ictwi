package org.bullit.dsictwi.similarity

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import kotlin.math.ln

data class Corpus(
    val idf: Map<String, Double>,
    val documentVectors: Map<String, RealVector>
)

fun buildCorpus(documents: List<String>, nGramSize: Int): Corpus {
    val docNGrams = documents.associateWith { extractNGrams(it, nGramSize) }
    val idf = calculateIdf(docNGrams.values)
    val vectors = calculateTfIdfVectors(docNGrams, idf)

    return Corpus(idf, vectors)
}

fun calculateIdf(allNGrams: Collection<List<String>>): Map<String, Double> {
    val totalDocs = allNGrams.size.toDouble()
    return allNGrams
        .flatMap { it.toSet() }
        .groupingBy { it }
        .eachCount()
        .mapValues { (_, count) ->
            ln((totalDocs + 1.0) / (count + 1.0)) + 1.0
        }
}

fun calculateTfIdfVectors(
    docNGrams: Map<String, List<String>>,
    idf: Map<String, Double>
): Map<String, RealVector> {
    return docNGrams.mapValues { (_, ngrams) ->
        val tf = ngrams.groupingBy { it }.eachCount()
            .mapValues { (_, count) -> count.toDouble() / ngrams.size }

        ArrayRealVector(
            idf.keys.map { ngram ->
                tf.getOrDefault(ngram, 0.0) * idf.getOrDefault(ngram, 0.0)
            }.toDoubleArray()
        )
    }
}

fun extractNGrams(text: String, nGramSize: Int): List<String> {
    // Split on whitespace, punctuation, or underscores
    val tokens = text.split("[\\s\\W_]+".toRegex())
        .filter { it.isNotBlank() }
        .map { it.lowercase() }

    return tokens.windowed(nGramSize, partialWindows = false) {
        it.joinToString(" ")
    }
}