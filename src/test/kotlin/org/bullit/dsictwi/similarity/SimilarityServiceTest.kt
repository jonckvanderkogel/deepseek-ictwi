package org.bullit.dsictwi.similarity

import arrow.core.Either
import org.apache.commons.math3.linear.ArrayRealVector
import org.bullit.dsictwi.error.MissingDocumentVector
import org.bullit.dsictwi.prompts.CodePair
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SimilarityServiceTest {
    private fun createSimpleCorpus(): Corpus {
        val idf = mapOf(
            "select" to 1.2,
            "from" to 0.8,
            "where" to 1.5
        )
        val documentVectors = mapOf(
            "doc1" to ArrayRealVector(doubleArrayOf(1.0, 0.5, 2.0)),
            "doc2" to ArrayRealVector(doubleArrayOf(0.5, 1.0, 0.0)),
            "doc3" to ArrayRealVector(doubleArrayOf(2.0, 0.0, 1.0))
        )
        return Corpus(idf, documentVectors)
    }

    private fun createThresholdTestCorpus(): Corpus {
        val idf = mapOf("token" to 1.0)
        val documentVectors = mapOf(
            "doc1" to ArrayRealVector(doubleArrayOf(0.9)),
            "doc2" to ArrayRealVector(doubleArrayOf(0.8)),
            "doc3" to ArrayRealVector(doubleArrayOf(0.7)),
            "doc4" to ArrayRealVector(doubleArrayOf(0.6)),
            "doc5" to ArrayRealVector(doubleArrayOf(0.4))
        )
        return Corpus(idf, documentVectors)
    }

    private fun createOrthogonalCorpus(): Corpus {
        val idf = mapOf("a" to 1.0, "b" to 1.0)
        val documentVectors = mapOf(
            "v1" to ArrayRealVector(doubleArrayOf(1.0, 0.0)),
            "v2" to ArrayRealVector(doubleArrayOf(0.0, 1.0))
        )
        return Corpus(idf, documentVectors)
    }

    private fun createService(corpus: Corpus, nGramSize: Int = 1): SimilarityService {
        return SimilarityService(corpus, nGramSize)
    }

    @Test
    fun `findSimilarExamples returns error when document vector is missing`() {
        val service = createService(createSimpleCorpus())
        val candidates = listOf(CodePair("invalid_doc", ""))

        val result = service
            .findSimilarExamples("select from where", candidates)

        assertTrue(result is Either.Left)
        result.mapLeft { errors ->
            assertTrue(errors.any { it is MissingDocumentVector })
        }
    }

    @Test
    fun `findSimilarExamples returns top matches with similarity threshold`() {
        val corpus = Corpus(
            idf = mapOf("a" to 1.0, "b" to 1.0),
            documentVectors = mapOf(
                "doc1" to ArrayRealVector(doubleArrayOf(3.0, 4.0)),
                "doc2" to ArrayRealVector(doubleArrayOf(1.0, 1.0)),
                "doc3" to ArrayRealVector(doubleArrayOf(0.5, 0.5)),
                "doc4" to ArrayRealVector(doubleArrayOf(0.3, 0.4)),
                "doc5" to ArrayRealVector(doubleArrayOf(0.5, -0.5))
            )
        )
        val service = createService(corpus, nGramSize = 1)

        val candidates = (1..5).map { CodePair("doc$it", "") }
        val result = service.findSimilarExamples("a b", candidates)

        assertTrue(result.isRight())
        result.map { examples ->
            assertEquals(4, examples.size)
            assertEquals(
                listOf("doc2", "doc3", "doc1", "doc4"),
                examples.map { it.plsql }
            )
        }
    }

    @Test
    fun `createVector generates correct TF-IDF values`() {
        val service = createService(createSimpleCorpus())
        val text = "select where"
        val vector = service.createVector(text, createSimpleCorpus())

        val expected = ArrayRealVector(doubleArrayOf(0.6, 0.0, 0.75))
        assertTrue(
            vector == expected,
            "Expected ${expected.toArray().contentToString()} but got ${vector.toArray().contentToString()}"
        )
    }
}