package com.ozd0.context2prompt.format

import org.junit.Assert.assertEquals
import org.junit.Test

class TreeFormatterTest {

    @Test
    fun `groups files by directory with full path`() {
        val result = TreeFormatter.format(
            listOf(
                "src/core/app-module/testing.ts",
                "src/core/app-module/types.ts",
                "src/core/tabs/index.ts",
            )
        )
        assertEquals(
            """
            src/core/app-module: testing.ts, types.ts
            src/core/tabs: index.ts
            """.trimIndent(),
            result.text
        )
        assertEquals(2, result.dirCount)
        assertEquals(3, result.fileCount)
    }

    @Test
    fun `root-level files get root marker`() {
        val result = TreeFormatter.format(listOf("readme.md", "src/Main.kt"))
        assertEquals(
            """
            (root): readme.md
            src: Main.kt
            """.trimIndent(),
            result.text
        )
        assertEquals(2, result.dirCount)
        assertEquals(2, result.fileCount)
    }

    @Test
    fun `directories sorted, files inside sorted`() {
        val result = TreeFormatter.format(
            listOf(
                "b/z.kt",
                "b/a.kt",
                "a/x.kt",
            )
        )
        assertEquals(
            """
            a: x.kt
            b: a.kt, z.kt
            """.trimIndent(),
            result.text
        )
    }

    @Test
    fun `duplicates collapsed`() {
        val result = TreeFormatter.format(listOf("a/b.kt", "a/b.kt"))
        assertEquals("a: b.kt", result.text)
        assertEquals(1, result.fileCount)
    }

    @Test
    fun `empty input gives empty output`() {
        val result = TreeFormatter.format(emptyList())
        assertEquals("", result.text)
        assertEquals(0, result.dirCount)
        assertEquals(0, result.fileCount)
    }

    @Test
    fun `empty dirs rendered with marker`() {
        val result = TreeFormatter.format(
            filePaths = listOf("src/Main.kt"),
            dirPaths = listOf("src/empty-module")
        )
        assertEquals(
            """
            src: Main.kt
            src/empty-module: (empty)
            """.trimIndent(),
            result.text
        )
        assertEquals(2, result.dirCount)
        assertEquals(1, result.fileCount)
    }

    @Test
    fun `dir that already has files not duplicated by dirPaths`() {
        val result = TreeFormatter.format(
            filePaths = listOf("src/Main.kt"),
            dirPaths = listOf("src", "src/")
        )
        assertEquals("src: Main.kt", result.text)
        assertEquals(1, result.dirCount)
    }

    @Test
    fun `trailing slash in dir path normalized`() {
        val result = TreeFormatter.format(
            filePaths = emptyList(),
            dirPaths = listOf("a/b/")
        )
        assertEquals("a/b: (empty)", result.text)
        assertEquals(1, result.dirCount)
        assertEquals(0, result.fileCount)
    }

    @Test
    fun `only empty dirs, no files`() {
        val result = TreeFormatter.format(
            filePaths = emptyList(),
            dirPaths = listOf("b", "a")
        )
        assertEquals(
            """
            a: (empty)
            b: (empty)
            """.trimIndent(),
            result.text
        )
        assertEquals(2, result.dirCount)
        assertEquals(0, result.fileCount)
    }

    @Test
    fun `counts dirs including empty and files`() {
        val result = TreeFormatter.format(
            filePaths = listOf("src/a.kt", "src/b.kt", "other/c.kt"),
            dirPaths = listOf("empty-dir")
        )
        assertEquals(3, result.dirCount)
        assertEquals(3, result.fileCount)
    }
}