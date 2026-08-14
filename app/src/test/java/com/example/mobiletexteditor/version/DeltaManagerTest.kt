package com.example.mobiletexteditor.version

import com.example.mobiletexteditor.version.data.FileVersionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeltaManagerTest {

    @Test
    fun testSingleDeltaCreationAndApplication() {
        val original = """
            fun main() {
                println("Hello, World!")
            }
        """.trimIndent()

        val revised = """
            fun main() {
                val greeting = "Hello, Mobile Text Editor!"
                println(greeting)
            }
        """.trimIndent()

        val deltaPatch = DeltaManager.createDelta(original, revised)
        assertTrue("Delta patch should not be blank", deltaPatch.isNotBlank())

        val applied = DeltaManager.applyDelta(original, deltaPatch)
        assertEquals("Reconstructed text must match revised text", revised, applied)
    }

    @Test
    fun testMultiVersionIncrementalReconstruction() {
        val v1Text = """
            # Project Notes
            - Task 1: Setup project
        """.trimIndent()

        val v2Text = """
            # Project Notes
            - Task 1: Setup project
            - Task 2: Implement delta versioning
        """.trimIndent()

        val v3Text = """
            # Project Notes
            - Task 1: Setup project (COMPLETED)
            - Task 2: Implement delta versioning (IN PROGRESS)
            - Task 3: Build Diff Viewer
        """.trimIndent()

        val v4Text = """
            # Modern Mobile Text Editor
            - Task 1: Setup project (COMPLETED)
            - Task 2: Implement delta versioning (COMPLETED)
            - Task 3: Build Diff Viewer (COMPLETED)
            - Task 4: Prepare Demo Video
        """.trimIndent()

        // Create simulated entity chain
        val versionsAsc = mutableListOf<FileVersionEntity>()

        // Version 1: Base version (raw text)
        versionsAsc.add(
            FileVersionEntity(
                id = 1,
                filePath = "/storage/notes.md",
                fileName = "notes.md",
                versionNumber = 1,
                versionName = "v1 - Initial",
                patchData = v1Text,
                isBaseVersion = true
            )
        )

        // Version 2: Delta from v1 -> v2
        val delta1To2 = DeltaManager.createDelta(v1Text, v2Text)
        versionsAsc.add(
            FileVersionEntity(
                id = 2,
                filePath = "/storage/notes.md",
                fileName = "notes.md",
                versionNumber = 2,
                versionName = "v2 - Added task 2",
                patchData = delta1To2,
                isBaseVersion = false
            )
        )

        // Version 3: Delta from v2 -> v3
        val delta2To3 = DeltaManager.createDelta(v2Text, v3Text)
        versionsAsc.add(
            FileVersionEntity(
                id = 3,
                filePath = "/storage/notes.md",
                fileName = "notes.md",
                versionNumber = 3,
                versionName = "v3 - Added task 3",
                patchData = delta2To3,
                isBaseVersion = false
            )
        )

        // Version 4: Delta from v3 -> v4
        val delta3To4 = DeltaManager.createDelta(v3Text, v4Text)
        versionsAsc.add(
            FileVersionEntity(
                id = 4,
                filePath = "/storage/notes.md",
                fileName = "notes.md",
                versionNumber = 4,
                versionName = "v4 - Finalized tasks",
                patchData = delta3To4,
                isBaseVersion = false
            )
        )

        // Verify reconstructing every version from the delta chain
        val reconstructedV1 = DeltaManager.reconstructVersion(versionsAsc, 1)
        assertEquals("Reconstructed v1 must match original v1", v1Text, reconstructedV1)

        val reconstructedV2 = DeltaManager.reconstructVersion(versionsAsc, 2)
        assertEquals("Reconstructed v2 must match original v2", v2Text, reconstructedV2)

        val reconstructedV3 = DeltaManager.reconstructVersion(versionsAsc, 3)
        assertEquals("Reconstructed v3 must match original v3", v3Text, reconstructedV3)

        val reconstructedV4 = DeltaManager.reconstructVersion(versionsAsc, 4)
        assertEquals("Reconstructed v4 must match original v4", v4Text, reconstructedV4)
    }

    @Test
    fun testLineChangesCalculation() {
        val base = "Line 1\nLine 2\nLine 3"
        val revised = "Line 1\nLine 2 (Modified)\nLine 3\nLine 4 (Added)"

        val (added, deleted) = DeltaManager.calculateLineChanges(base, revised)
        assertTrue("Added lines count should be > 0", added > 0)
    }
}
