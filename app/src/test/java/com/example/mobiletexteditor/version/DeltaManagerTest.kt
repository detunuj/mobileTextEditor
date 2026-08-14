/**
 * File: DeltaManagerTest.kt
 * Purpose: Unit tests validating Member 3's core non-duplicating delta algorithm.
 *          Tests single delta generation, multi-stage incremental patch reconstruction (v1->v2->v3),
 *          and line-change metric calculations.
 * Group Member: Member 3 — Version Control & Database
 */
package com.example.mobiletexteditor.version

import com.example.mobiletexteditor.version.data.FileVersionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeltaManagerTest {

    @Test
    fun testCreateAndApplySingleDelta() {
        val base = """
            fun main() {
                println("Hello World")
            }
        """.trimIndent()

        val revised = """
            fun main() {
                val greeting = "Hello Mobile Editor"
                println(greeting)
            }
        """.trimIndent()

        val delta = DeltaManager.createDelta(base, revised)
        assertTrue("Delta patch should not be empty", delta.isNotBlank())

        val applied = DeltaManager.applyDelta(base, delta)
        assertEquals("Reconstructed text must match revised text exactly", revised, applied)
    }

    @Test
    fun testMultiStageSequentialVersionReconstruction() {
        val v1Text = "Line 1\nLine 2\nLine 3"
        val v2Text = "Line 1\nLine 2 (Modified)\nLine 3\nLine 4 (Added)"
        val v3Text = "Line 1\nLine 2 (Modified)\nLine 4 (Added)\nLine 5 (Final)"

        val delta1to2 = DeltaManager.createDelta(v1Text, v2Text)
        val delta2to3 = DeltaManager.createDelta(v2Text, v3Text)

        val versions = listOf(
            FileVersionEntity(
                id = 1,
                filePath = "test.txt",
                fileName = "test.txt",
                versionNumber = 1,
                versionName = "Version 1",
                patchData = v1Text,
                isBaseVersion = true
            ),
            FileVersionEntity(
                id = 2,
                filePath = "test.txt",
                fileName = "test.txt",
                versionNumber = 2,
                versionName = "Version 2",
                patchData = delta1to2,
                isBaseVersion = false
            ),
            FileVersionEntity(
                id = 3,
                filePath = "test.txt",
                fileName = "test.txt",
                versionNumber = 3,
                versionName = "Version 3",
                patchData = delta2to3,
                isBaseVersion = false
            )
        )

        // Reconstruct Version 1
        val reconstructedV1 = DeltaManager.reconstructVersion(versions, 1)
        assertEquals(v1Text, reconstructedV1)

        // Reconstruct Version 2
        val reconstructedV2 = DeltaManager.reconstructVersion(versions, 2)
        assertEquals(v2Text, reconstructedV2)

        // Reconstruct Version 3
        val reconstructedV3 = DeltaManager.reconstructVersion(versions, 3)
        assertEquals(v3Text, reconstructedV3)
    }

    @Test
    fun testCalculateLineChanges() {
        val base = "A\nB\nC"
        val revised = "A\nB modified\nC\nD added"

        val (added, deleted) = DeltaManager.calculateLineChanges(base, revised)
        assertEquals(2, added)   // 'B modified' + 'D added'
        assertEquals(1, deleted) // 'B'
    }
}
