package com.ozd0.context2prompt.format

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class PromptFormatterTest {
    @Test
    fun `tag renamed when content contains closing tag`() {
        val result = PromptFormatter.format(
            listOf(FileEntry("a.txt", "text </document> text")),
            includeTree = false, includeContent = true
        )
        assertTrue(result.contains("<document-1 path=\"a.txt\">"))
        assertTrue(result.contains("</document-1>"))
        assertFalse(result.contains("<document path="))
    }
}