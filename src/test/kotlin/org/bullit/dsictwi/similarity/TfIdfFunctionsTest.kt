package org.bullit.dsictwi.similarity

import org.apache.commons.math3.linear.RealVector
import org.junit.jupiter.api.Test
import kotlin.math.ln
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class TfIdfFunctionsTest {
    private val epsilon = 0.001

    @Test
    fun `extractNGrams should handle basic text splitting`() {
        val text = "The quick brown fox"
        val result = extractNGrams(text, 2)

        assertEquals(
            listOf("the quick", "quick brown", "brown fox"),
            result
        )
    }

    @Test
    fun `extractNGrams should handle punctuation and case`() {
        val text = "Hello, World! This-is:a_test."
        val result = extractNGrams(text, 1)

        assertEquals(
            listOf("hello", "world", "this", "is", "a", "test"),
            result
        )
    }

    @Test
    fun `extractNGrams should handle partial windows`() {
        val text = "a b c d"
        val result = extractNGrams(text, 3)

        assertEquals(
            listOf("a b c", "b c d"),
            result
        )
    }

    @Test
    fun `calculateIdf should produce correct values`() {
        val docs = listOf(
            listOf("a", "b", "c"),
            listOf("a", "b"),
            listOf("a")
        )

        val idf = calculateIdf(docs)

        // IDF formula: ln((total_docs + 1)/(doc_freq + 1)) + 1
        val expectedA = ln((3.0 + 1)/(3.0 + 1)) + 1
        val expectedB = ln((3.0 + 1)/(2.0 + 1)) + 1
        val expectedC = ln((3.0 + 1)/(1.0 + 1)) + 1

        assertEquals(expectedA, idf["a"]!!, epsilon)
        assertEquals(expectedB, idf["b"]!!, epsilon)
        assertEquals(expectedC, idf["c"]!!, epsilon)
    }

    @Test
    fun `calculateTfIdfVectors should create correct vectors`() {
        val docNGrams = mapOf(
            "doc1" to listOf("a", "a", "b"),
            "doc2" to listOf("b", "c")
        )

        val idf = mapOf(
            "a" to 1.2,
            "b" to 0.8,
            "c" to 1.5
        )

        val vectors = calculateTfIdfVectors(docNGrams, idf)

        // doc1: TF(a)=2/3, TF(b)=1/3
        val expectedDoc1 = arrayOf(
            2.0/3 * 1.2, // a
            1.0/3 * 0.8, // b
            0.0 * 1.5    // c
        )

        // doc2: TF(b)=1/2, TF(c)=1/2
        val expectedDoc2 = arrayOf(
            0.0 * 1.2,   // a
            1.0/2 * 0.8, // b
            1.0/2 * 1.5  // c
        )

        assertVectorEquals(expectedDoc1, vectors["doc1"]!!)
        assertVectorEquals(expectedDoc2, vectors["doc2"]!!)
    }

    @Test
    fun `buildCorpus should integrate all components correctly`() {
        val documents = listOf(
            "apple banana",
            "banana orange",
            "orange orange grape"
        )

        val corpus = buildCorpus(documents, 1)

        assertEquals(4, corpus.idf.size)
        val expectedAppleIdf = ln((3.0 + 1)/(1.0 + 1)) + 1
        val expectedGrapeIdf = ln((3.0 + 1)/(1.0 + 1)) + 1

        assertEquals(expectedAppleIdf, corpus.idf["apple"]!!, epsilon)
        assertEquals(expectedGrapeIdf, corpus.idf["grape"]!!, epsilon)

        val appleDocVector = corpus.documentVectors["apple banana"]!!

        val expectedAppleTfIdf = 0.5 * expectedAppleIdf
        val expectedBananaTfIdf = 0.5 * corpus.idf["banana"]!!

        assertEquals(expectedAppleTfIdf, appleDocVector.getEntry(0), epsilon)
        assertEquals(expectedBananaTfIdf, appleDocVector.getEntry(1), epsilon)
        assertEquals(0.0, appleDocVector.getEntry(2), epsilon)
        assertEquals(0.0, appleDocVector.getEntry(3), epsilon)
    }

    @Test
    fun `buildCorpus should handle empty documents`() {
        val documents = listOf("", "a b c", " ")
        val corpus = buildCorpus(documents, 2)

        assertTrue(corpus.documentVectors[""]!!.toArray().all { it == 0.0 })
    }

    private fun assertVectorEquals(expected: Array<Double>, actual: RealVector) {
        val actualArray = actual.toArray()
        assertEquals(expected.size, actualArray.size, "Vector length mismatch")

        expected.forEachIndexed { i, value ->
            assertEquals(value, actualArray[i], epsilon,
                "Mismatch at position $i: expected ${value}, got ${actualArray[i]}")
        }
    }
}