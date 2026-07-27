package com.ozd0.context2prompt.core

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.codeInsight.daemon.impl.HighlightInfo

data class Problem(
    val severity: String,
    val description: String,
    val line: Int,
    val column: Int,
    val codeLine: String,
)

object ProblemCollector {

    fun collect(
        project: Project,
        document: Document,
        minSeverity: HighlightSeverity = HighlightSeverity.WARNING,
    ): List<Problem> = collectFiltered(project, document, minSeverity) { true }

    fun collectAtLine(
        project: Project,
        document: Document,
        offset: Int,
        minSeverity: HighlightSeverity = HighlightSeverity.WARNING,
    ): List<Problem> = runReadAction {
        val caretLine = document.getLineNumber(offset)
        collectInReadAction(project, document, minSeverity) { info ->
            document.getLineNumber(info.startOffset) == caretLine
        }
    }

    private fun collectFiltered(
        project: Project,
        document: Document,
        minSeverity: HighlightSeverity,
        filter: (HighlightInfo) -> Boolean,
    ): List<Problem> = runReadAction { collectInReadAction(project, document, minSeverity, filter) }

    private fun collectInReadAction(
        project: Project,
        document: Document,
        minSeverity: HighlightSeverity,
        filter: (HighlightInfo) -> Boolean,
    ): List<Problem> {
        val markup = DocumentMarkupModel.forDocument(document, project, false) ?: return emptyList()
        return markup.allHighlighters
            .mapNotNull { HighlightInfo.fromRangeHighlighter(it) }
            .filter { it.severity >= minSeverity && it.description != null && filter(it) }
            .map { toProblem(it, document) }
            .sortedWith(compareBy({ it.line }, { it.column }))
            .distinctBy { it.line to it.description }
    }

    private fun toProblem(info: HighlightInfo, document: Document): Problem {
        val line = document.getLineNumber(info.startOffset)
        val col = info.startOffset - document.getLineStartOffset(line)
        val codeLine = document.getText(
            TextRange(document.getLineStartOffset(line), document.getLineEndOffset(line))
        )
        return Problem(
            severity = info.severity.name,
            description = info.description ?: "",
            line = line + 1,
            column = col + 1,
            codeLine = codeLine.trim(),
        )
    }
}
