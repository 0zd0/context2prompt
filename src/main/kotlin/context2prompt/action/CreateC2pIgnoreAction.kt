package com.ozd0.context2prompt.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.guessProjectDir
import com.ozd0.context2prompt.PluginConstants

class CreateC2pIgnoreAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val baseDir = project.guessProjectDir() ?: return

        val existing = baseDir.findChild(PluginConstants.C2PIGNORE_FILE)
        val file = existing ?: WriteAction.compute<_, RuntimeException> {
            baseDir.createChildData(this, PluginConstants.C2PIGNORE_FILE).also {
                it.setBinaryContent(TEMPLATE.toByteArray())
            }
        }
        FileEditorManager.getInstance(project).openFile(file, true)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private companion object {
        val TEMPLATE = """
            # Context2Prompt ignore rules (gitignore syntax).
            # Applied on top of .gitignore; use !pattern to re-include ignored entries.

        """.trimIndent()
    }
}
