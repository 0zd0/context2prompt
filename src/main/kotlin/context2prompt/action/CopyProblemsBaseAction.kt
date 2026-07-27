package com.ozd0.context2prompt.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.ozd0.context2prompt.core.FileCollector
import com.ozd0.context2prompt.core.ProblemCollector
import com.ozd0.context2prompt.core.TokenEstimator
import com.ozd0.context2prompt.format.FileEntry
import com.ozd0.context2prompt.format.ProblemFormatter
import com.ozd0.context2prompt.format.PromptFormatter
import com.ozd0.context2prompt.ui.PluginUi
import java.awt.datatransfer.StringSelection

abstract class CopyProblemsBaseAction(
    private val includeContent: Boolean,
    private val lineOnly: Boolean,
) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val problems = if (lineOnly) {
            ProblemCollector.collectAtLine(project, editor.document, editor.caretModel.offset)
        } else {
            ProblemCollector.collect(project, editor.document)
        }
        if (problems.isEmpty()) {
            PluginUi.notifyNoProblems(project)
            return
        }

        val collector = FileCollector.create(project)
        val path = collector.relativeToContentRoot(vf) ?: vf.name

        val sb = StringBuilder(ProblemFormatter.format(path, problems))
        if (includeContent) {
            sb.append('\n')
            sb.append(
                PromptFormatter.format(
                    listOf(FileEntry(path, editor.document.text)),
                    includeTree = false, includeContent = true,
                )
            )
        }
        val text = sb.toString()
        CopyPasteManager.getInstance().setContents(StringSelection(text))
        PluginUi.notifyProblemsCopied(project, problems.size, TokenEstimator.estimate(text))
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
            e.project != null && e.getData(CommonDataKeys.EDITOR) != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
