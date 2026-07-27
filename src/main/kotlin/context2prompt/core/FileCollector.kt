package com.ozd0.context2prompt.core

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.ozd0.context2prompt.PluginConstants

class CollectResult(
    val files: List<VirtualFile>,
    val emptyDirs: List<String>,
    val skippedCount: Int,
)

class FileCollector(
    private val project: Project,
    private val matcher: IgnoreMatcher,
    private val maxFileSize: Long = MAX_FILE_SIZE,
) {
    private val fileIndex = ProjectFileIndex.getInstance(project)

    fun splitSelectionByIgnoreStatus(selected: List<VirtualFile>): SplitSelection<VirtualFile> {
        val normalItems = mutableListOf<SelectedItem<VirtualFile>>()
        val ignoredItems = mutableListOf<SelectedItem<VirtualFile>>()

        for (selectedFile in selected) {
            val selectedPath = relativeToContentRoot(selectedFile) ?: continue

            val pushedLayerDirs = mutableListOf<String>()
            for (ancestorDir in ancestorDirsFromContentRoot(selectedFile)) {
                val ancestorPath = relativeToContentRoot(ancestorDir) ?: continue
                val gitignoreContent = readChildFileText(ancestorDir, PluginConstants.GITIGNORE_FILE)
                val c2pignoreContent = readChildFileText(ancestorDir, PluginConstants.C2PIGNORE_FILE)
                if (gitignoreContent != null || c2pignoreContent != null) {
                    gitignoreContent?.let { matcher.pushNestedGitignore(ancestorPath, it) }
                    c2pignoreContent?.let { matcher.pushNestedC2pignore(ancestorPath, it) }
                    pushedLayerDirs.add(ancestorPath)
                }
            }

            val isIgnored = matcher.isIgnored(selectedPath, selectedFile.isDirectory)
            pushedLayerDirs.asReversed().forEach { matcher.popNested(it) }

            val item = SelectedItem(selectedFile, selectedPath, selectedFile.isDirectory)
            if (isIgnored) ignoredItems.add(item) else normalItems.add(item)
        }
        return SplitSelection(normalItems, ignoredItems)
    }

    private fun ancestorDirsFromContentRoot(file: VirtualFile): List<VirtualFile> =
        generateSequence(file.parent) { it.parent }
            .takeWhile { it.isDirectory && relativeToContentRoot(it)?.isNotEmpty() == true }
            .toList()
            .asReversed()

    /**
     * Collection based on established semantics:
     * - normal-roots: traversal with ignore-filtering (nested files are included);
     * - ignored roots: if includeIgnored=true — traversal WITHOUT ignore-filtering,
     *   if false — skipped entirely;
     * - binary/size filters always apply.
     */
    fun collect(split: SplitSelection<VirtualFile>, includeIgnored: Boolean): CollectResult {
        val files = mutableListOf<VirtualFile>()
        val emptyDirs = mutableListOf<String>()
        var skipped = 0

        for ((payload) in split.normal) {
            walk(payload, applyIgnore = true, files, emptyDirs) { skipped++ }
        }
        if (includeIgnored) {
            for ((payload) in split.ignored) {
                walk(payload, applyIgnore = false, files, emptyDirs) { skipped++ }
            }
        } else {
            skipped += split.ignored.size
        }
        return CollectResult(files, emptyDirs, skipped)
    }

    private fun walk(
        vf: VirtualFile,
        applyIgnore: Boolean,
        files: MutableList<VirtualFile>,
        emptyDirs: MutableList<String>,
        onSkipped: () -> Unit,
    ) {
        if (!vf.isDirectory) {
            collectFile(vf, files, onSkipped)
            return
        }

        val dirRel = relativeToContentRoot(vf) ?: return

        val nestedGit = if (applyIgnore) readChildFileText(vf, PluginConstants.GITIGNORE_FILE) else null
        val nestedC2p = if (applyIgnore) readChildFileText(vf, PluginConstants.C2PIGNORE_FILE) else null
        nestedGit?.let { matcher.pushNestedGitignore(dirRel, it) }
        nestedC2p?.let { matcher.pushNestedC2pignore(dirRel, it) }

        try {
            val children = vf.children
            if (children.isEmpty()) {
                emptyDirs.add(dirRel)
                return
            }
            for (child in children) {
                val rel = relativeToContentRoot(child) ?: continue
                if (applyIgnore && matcher.isIgnored(rel, child.isDirectory)) {
                    onSkipped()
                    continue
                }
                if (child.isDirectory) {
                    if (applyIgnore && isExcluded(child)) {
                        onSkipped()
                        continue
                    }
                    walk(child, applyIgnore, files, emptyDirs, onSkipped)
                } else {
                    collectFile(child, files, onSkipped)
                }
            }
        } finally {
            if (nestedGit != null || nestedC2p != null) matcher.popNested(dirRel)
        }
    }

    private fun collectFile(vf: VirtualFile, files: MutableList<VirtualFile>, onSkipped: () -> Unit) {
        if (vf.length > maxFileSize) { onSkipped(); return }
        if (vf.fileType.isBinary) { onSkipped(); return }
        files.add(vf)
    }

    fun relativeToContentRoot(vf: VirtualFile): String? = runReadAction {
        val root = fileIndex.getContentRootForFile(vf) ?: return@runReadAction vf.name
        VfsUtilCore.getRelativePath(vf, root)
    }

    private fun isExcluded(vf: VirtualFile): Boolean =
        runReadAction { fileIndex.isExcluded(vf) }

    private fun readChildFileText(dir: VirtualFile, name: String): String? =
        dir.findChild(name)
            ?.takeIf { it.isValid && !it.isDirectory }
            ?.let { String(it.contentsToByteArray(), it.charset) }

    companion object {
        const val MAX_FILE_SIZE: Long = 1L * 1024 * 1024

        fun create(project: Project): FileCollector {
            val basePath = project.guessProjectDir()
            fun read(name: String): String? = basePath?.findChild(name)
                ?.takeIf { it.isValid && !it.isDirectory }
                ?.let { String(it.contentsToByteArray(), it.charset) }
            return FileCollector(project, IgnoreMatcher(read(PluginConstants.GITIGNORE_FILE), read(PluginConstants.C2PIGNORE_FILE)))
        }
    }
}
