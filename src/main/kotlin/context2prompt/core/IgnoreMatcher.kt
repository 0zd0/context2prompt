package com.ozd0.context2prompt.core

import org.eclipse.jgit.ignore.IgnoreNode
import java.io.ByteArrayInputStream

class IgnoreMatcher(
    rootGitignore: String?,
    rootC2pignore: String?,
) {
    private class Layer(val node: IgnoreNode, val baseDir: String)

    private val gitLayers = ArrayDeque<Layer>()
    private val c2pLayers = ArrayDeque<Layer>()

    init {
        gitLayers.addLast(Layer(parse(DEFAULT_PATTERNS), ""))
        rootGitignore?.let { gitLayers.addLast(Layer(parse(it), "")) }
        rootC2pignore?.let { c2pLayers.addLast(Layer(parse(it), "")) }
    }

    fun pushNestedGitignore(dirRelativePath: String, content: String) {
        gitLayers.addLast(Layer(parse(content), dirRelativePath))
    }

    fun pushNestedC2pignore(dirRelativePath: String, content: String) {
        c2pLayers.addLast(Layer(parse(content), dirRelativePath))
    }

    fun popNested(dirRelativePath: String) {
        if (c2pLayers.lastOrNull()?.baseDir == dirRelativePath) c2pLayers.removeLast()
        if (gitLayers.lastOrNull()?.baseDir == dirRelativePath) gitLayers.removeLast()
    }

    fun isIgnored(relativePath: String, isDirectory: Boolean): Boolean {
        match(c2pLayers, relativePath, isDirectory)?.let { return it }
        match(gitLayers, relativePath, isDirectory)?.let { return it }
        return false
    }

    private fun match(layers: ArrayDeque<Layer>, path: String, isDirectory: Boolean): Boolean? {
        for (layer in layers.asReversed()) {
            val localPath = relativeTo(path, layer.baseDir) ?: continue
            when (layer.node.isIgnored(localPath, isDirectory)) {
                IgnoreNode.MatchResult.IGNORED -> return true
                IgnoreNode.MatchResult.NOT_IGNORED -> return false
                else -> {}
            }
        }
        return null
    }

    private fun relativeTo(path: String, baseDir: String): String? {
        if (baseDir.isEmpty()) return path
        val prefix = "$baseDir/"
        return if (path.startsWith(prefix)) path.removePrefix(prefix) else null
    }

    private companion object {
        const val DEFAULT_PATTERNS = ".git/\n.idea/"

        fun parse(content: String): IgnoreNode =
            IgnoreNode().apply { parse(ByteArrayInputStream(content.toByteArray())) }
    }
}
