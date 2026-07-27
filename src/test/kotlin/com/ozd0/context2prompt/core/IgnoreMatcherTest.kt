package com.ozd0.context2prompt.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IgnoreMatcherTest {

    @Test
    fun `gitignore patterns work`() {
        val m = IgnoreMatcher("node_modules/\n*.log", null)
        assertTrue(m.isIgnored("node_modules", true))
        assertTrue(m.isIgnored("app.log", false))
        assertFalse(m.isIgnored("src/Main.kt", false))
    }

    @Test
    fun `star does not cross slash, double star does`() {
        val m = IgnoreMatcher("src/*.tmp\ndist/**", null)
        assertTrue(m.isIgnored("src/a.tmp", false))
        assertFalse(m.isIgnored("src/deep/a.tmp", false))
        assertTrue(m.isIgnored("dist/deep/bundle.js", false))
    }

    @Test
    fun `dir pattern does not match same-name file`() {
        val m = IgnoreMatcher("build/", null)
        assertTrue(m.isIgnored("build", true))
        assertFalse(m.isIgnored("build", false))
    }

    @Test
    fun `root c2pignore adds pattern over root gitignore`() {
        val m = IgnoreMatcher("node_modules/", "*.lock")
        assertTrue(m.isIgnored("yarn.lock", false))
        assertTrue(m.isIgnored("node_modules", true))
    }

    @Test
    fun `root c2pignore negation overrides root gitignore`() {
        val m = IgnoreMatcher("generated/", "!generated/")
        assertFalse(m.isIgnored("generated", true))
    }

    @Test
    fun `default patterns always ignored`() {
        val m = IgnoreMatcher(null, null)
        assertTrue(m.isIgnored(".git", true))
        assertTrue(m.isIgnored(".idea", true))
    }

    @Test
    fun `nested gitignore scoped to subtree and negation overrides root`() {
        val m = IgnoreMatcher("*.log", null)
        m.pushNestedGitignore("sub", "!keep.log")
        assertFalse(m.isIgnored("sub/keep.log", false))
        assertTrue(m.isIgnored("sub/other.log", false))
        assertTrue(m.isIgnored("outside.log", false))
        m.popNested("sub")
        assertTrue(m.isIgnored("sub/keep.log", false))
    }

    @Test
    fun `deeper gitignore overrides shallower gitignore`() {
        val m = IgnoreMatcher(null, null)
        m.pushNestedGitignore("sub", "*.log")
        m.pushNestedGitignore("sub/deep", "!keep.log")
        assertFalse(m.isIgnored("sub/deep/keep.log", false))
        assertTrue(m.isIgnored("sub/other.log", false))
    }

    @Test
    fun `nested c2pignore applies only to its subtree`() {
        val m = IgnoreMatcher(null, null)
        m.pushNestedC2pignore("sub", "*.snap")
        assertTrue(m.isIgnored("sub/a.snap", false))
        assertFalse(m.isIgnored("other/a.snap", false))
        m.popNested("sub")
        assertFalse(m.isIgnored("sub/a.snap", false))
    }

    @Test
    fun `nested c2pignore negation overrides nested gitignore`() {
        val m = IgnoreMatcher(null, null)
        m.pushNestedGitignore("sub", "*.log")
        m.pushNestedC2pignore("sub", "!keep.log")
        assertFalse(m.isIgnored("sub/keep.log", false))
        assertTrue(m.isIgnored("sub/other.log", false))
    }

    @Test
    fun `root c2pignore overrides nested gitignore`() {
        val m = IgnoreMatcher(null, "!important/")
        m.pushNestedGitignore("sub", "important/")
        assertFalse(m.isIgnored("sub/important", true))
    }

    @Test
    fun `deeper c2pignore overrides shallower c2pignore`() {
        val m = IgnoreMatcher(null, "*.md")
        m.pushNestedC2pignore("docs", "!readme.md")
        assertFalse(m.isIgnored("docs/readme.md", false))
        assertTrue(m.isIgnored("docs/other.md", false))
        assertTrue(m.isIgnored("top.md", false))
    }

    @Test
    fun `popNested removes both stacks pushed for same dir`() {
        val m = IgnoreMatcher(null, null)
        m.pushNestedGitignore("sub", "*.log")
        m.pushNestedC2pignore("sub", "*.tmp")
        m.popNested("sub")
        assertFalse(m.isIgnored("sub/a.log", false))
        assertFalse(m.isIgnored("sub/a.tmp", false))
    }

    @Test
    fun `popNested for dir without pushed layers is no-op`() {
        val m = IgnoreMatcher("*.log", null)
        m.popNested("whatever")
        assertTrue(m.isIgnored("a.log", false))
    }

    @Test
    fun `comments and blank lines in ignore content are skipped`() {
        val m = IgnoreMatcher("# comment\n\n*.log\n", null)
        assertTrue(m.isIgnored("a.log", false))
        assertFalse(m.isIgnored("# comment", false))
    }

    @Test
    fun `anchored pattern relative to its layer base dir`() {
        val m = IgnoreMatcher(null, null)
        m.pushNestedGitignore("sub", "/build")
        assertTrue(m.isIgnored("sub/build", true))
        assertFalse(m.isIgnored("sub/nested/build", true))
    }

    @Test
    fun `file inside ignored dir is ignored`() {
        val m = IgnoreMatcher("certs/", null)
        assertTrue(m.isIgnored("certs/root_ca.pem", false))
        assertTrue(m.isIgnored("certs/deep/file.pem", false))
        assertFalse(m.isIgnored("other/root_ca.pem", false))
    }

    @Test
    fun `nested gitignore dir pattern ignores deep file on direct check`() {
        val m = IgnoreMatcher(null, null)
        m.pushNestedGitignore("python", "certs/")
        assertTrue(m.isIgnored("python/certs/root_ca.pem", false))
        assertFalse(m.isIgnored("python/other.txt", false))
    }

    @Test
    fun `negation cannot re-include file inside ignored dir`() {
        val m = IgnoreMatcher("build/\n!build/keep.txt", null)
        assertTrue(m.isIgnored("build/keep.txt", false))
    }

    @Test
    fun `negation on dir itself re-includes subtree`() {
        val m = IgnoreMatcher("generated/", "!generated/")
        assertFalse(m.isIgnored("generated/file.txt", false))
    }
}
