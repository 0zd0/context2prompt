package com.ozd0.context2prompt.action

import com.ozd0.context2prompt.core.FileCollector
import com.ozd0.context2prompt.format.TreeFormatter
import com.ozd0.context2prompt.ui.PluginUi
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.ozd0.context2prompt.core.TokenEstimator
import java.awt.datatransfer.StringSelection

class CopyTreeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selected = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList() ?: return
        if (selected.isEmpty()) return

        val collector = FileCollector.create(project)
        val split = collector.splitSelectionByIgnoreStatus(selected)

        val includeIgnored =
            split.hasIgnored && (PluginUi.askIncludeIgnored(project, split.ignored) ?: return)

        val result = collector.collect(split, includeIgnored)
        val paths = result.files.mapNotNull { collector.relativeToContentRoot(it) }

        val tree = TreeFormatter.format(paths, result.emptyDirs)
        CopyPasteManager.getInstance().setContents(StringSelection(tree.text))
        PluginUi.notifyStructureCopied(project, tree.dirCount, tree.fileCount, TokenEstimator.estimate(tree.text), result.skippedCount)
    }

    override fun update(e: AnActionEvent) {
        val selected = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        e.presentation.isEnabledAndVisible = e.project != null && !selected.isNullOrEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
