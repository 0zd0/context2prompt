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

class CopyTreeAction : SelectionCopyAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        CopyPipeline.copyTree(project, selection(e))
    }
}
