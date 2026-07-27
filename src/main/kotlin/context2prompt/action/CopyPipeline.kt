package com.ozd0.context2prompt.action

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.ozd0.context2prompt.Context2PromptBundle
import com.ozd0.context2prompt.core.FileCollector
import com.ozd0.context2prompt.core.FileContentReader
import com.ozd0.context2prompt.core.TokenEstimator
import com.ozd0.context2prompt.format.FileEntry
import com.ozd0.context2prompt.format.PromptFormatter
import com.ozd0.context2prompt.format.TreeFormatter
import com.ozd0.context2prompt.ui.PluginUi
import java.awt.datatransfer.StringSelection

object CopyPipeline {

    private class Context(
        val files: List<VirtualFile>,
        val emptyDirs: List<String>,
        val skippedCount: Int,
        val collector: FileCollector,
    )

    /** null - user cancellation */
    private fun collect(project: Project, selected: List<VirtualFile>): Context? {
        if (selected.isEmpty()) return null
        val collector = FileCollector.create(project)
        val split = collector.splitSelectionByIgnoreStatus(selected)
        val includeIgnored = split.hasIgnored &&
                (PluginUi.askIncludeIgnored(project, split.ignored) ?: return null)
        val result = collector.collect(split, includeIgnored)
        return Context(result.files, result.emptyDirs, result.skippedCount, collector)
    }

    fun copyPrompt(project: Project, selected: List<VirtualFile>) {
        val ctx = collect(project, selected) ?: return

        val entries = ProgressManager.getInstance().runProcessWithProgressSynchronously<List<FileEntry>, RuntimeException>(
            {
                runReadAction {
                    ctx.files.mapNotNull { vf ->
                        ctx.collector.relativeToContentRoot(vf)
                            ?.let { FileEntry(it, FileContentReader.read(vf)) }
                    }
                }
            },
            Context2PromptBundle.message("progress.collecting"),
            true,
            project,
        ) ?: return
        if (entries.isEmpty()) return

        val prompt = PromptFormatter.format(entries, includeTree = true, includeContent = true)
        copy(prompt)
        PluginUi.notifyCopied(project, entries.size, ctx.skippedCount, TokenEstimator.estimate(prompt))
    }

    fun copyTree(project: Project, selected: List<VirtualFile>) {
        val ctx = collect(project, selected) ?: return
        val paths = ctx.files.mapNotNull { ctx.collector.relativeToContentRoot(it) }
        val tree = TreeFormatter.format(paths, ctx.emptyDirs)
        copy(tree.text)
        PluginUi.notifyStructureCopied(
            project, tree.dirCount, tree.fileCount,
            TokenEstimator.estimate(tree.text), ctx.skippedCount,
        )
    }

    private fun copy(text: String) =
        CopyPasteManager.getInstance().setContents(StringSelection(text))
}
