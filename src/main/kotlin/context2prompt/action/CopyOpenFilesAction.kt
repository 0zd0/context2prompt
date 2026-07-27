package com.ozd0.context2prompt.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.ozd0.context2prompt.core.FileCollector
import com.ozd0.context2prompt.core.FileContentReader
import com.ozd0.context2prompt.core.TokenEstimator
import com.ozd0.context2prompt.format.FileEntry
import com.ozd0.context2prompt.format.PromptFormatter
import com.ozd0.context2prompt.ui.PluginUi
import java.awt.datatransfer.StringSelection

class CopyOpenFilesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val openFiles = FileEditorManager.getInstance(project).openFiles.toList()
        if (openFiles.isEmpty()) return

        val collector = FileCollector.create(project)
        val split = collector.splitSelectionByIgnoreStatus(openFiles)

        val includeIgnored =
            split.hasIgnored && (PluginUi.askIncludeIgnored(project, split.ignored) ?: return)

        val result = collector.collect(split, includeIgnored)
        val entries = result.files.mapNotNull { vf ->
            collector.relativeToContentRoot(vf)?.let { FileEntry(it, FileContentReader.read(vf)) }
        }
        if (entries.isEmpty()) return

        val prompt = PromptFormatter.format(entries, includeTree = true, includeContent = true)
        CopyPasteManager.getInstance().setContents(StringSelection(prompt))
        PluginUi.notifyCopied(project, entries.size, result.skippedCount, TokenEstimator.estimate(prompt))
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible =
            project != null && FileEditorManager.getInstance(project).openFiles.isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
