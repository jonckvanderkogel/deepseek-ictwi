package org.bullit.dsictwi.similarity

import arrow.core.Either
import arrow.core.nel
import arrow.core.raise.either
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.bullit.dsictwi.error.ApplicationErrors
import org.bullit.dsictwi.error.MissingDocumentVector
import org.bullit.dsictwi.prompts.CodePair

class SimilarityService(
    private val corpus: Corpus,
    private val nGramSize: Int
) {
    fun findSimilarExamples(
        targetPlsql: String,
        candidates: List<CodePair>
    ): Either<ApplicationErrors, List<CodePair>> = either {
        val targetVector = createVector(targetPlsql, corpus)

        candidates
            .map { example ->
                val exampleVector = fetchDocumentVector(corpus, example.plsql).bind()
                example to cosineSimilarity(targetVector, exampleVector)
            }
            .sortedByDescending { (_, similarity) -> similarity }
            .filterIndexed { index, (_, similarity) ->
                index < 3 || similarity >= 0.5
            }
            .map { (example, _) -> example }
    }

    private fun fetchDocumentVector(corpus: Corpus, plsql: String): Either<ApplicationErrors, RealVector> = either {
        corpus.documentVectors[plsql]
            ?: raise(MissingDocumentVector(plsql.take(30) + "...").nel())
    }

    fun createVector(text: String, corpus: Corpus): RealVector {
        val ngrams = extractNGrams(text, nGramSize)
        val tf = ngrams
            .groupingBy { it }
            .eachCount()
            .mapValues { (_, count) -> count.toDouble() / ngrams.size }

        return ArrayRealVector(
            corpus
                .idf
                .keys
                .map { ngram -> tf[ngram]?.let { it * corpus.idf[ngram]!! } ?: 0.0 }
                .toDoubleArray())
    }

    private fun cosineSimilarity(v1: RealVector, v2: RealVector): Double {
        val sim =  v1.cosine(v2)
        return sim
    }
}