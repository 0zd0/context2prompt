package com.ozd0.context2prompt.action

import com.intellij.openapi.actionSystem.AnActionEvent

class CopyContextAction : SelectionCopyAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        CopyPipeline.copyPrompt(project, selection(e))
    }
}