package com.ozd0.context2prompt.core

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

data class Problem(
    val severity: String,      // ERROR / WARNING
    val description: String,
    val line: Int,             // 1-based
    val column: Int,           // 1-based
    val codeLine: String,
)

object ProblemCollector {

    fun collect(project: Project, document: Document, minSeverity: HighlightSeverity = HighlightSeverity.WARNING): List<Problem> =
        runReadAction {
            val problems = mutableListOf<Problem>()
            DaemonCodeAnalyzerImpl.processHighlights(
                document, project, minSeverity, 0, document.textLength
            ) { info: HighlightInfo ->
                problems.add(toProblem(info, document))
                true
            }
            dedup(problems)
        }

    fun collectAtLine(project: Project, document: Document, offset: Int): List<Problem> =
        runReadAction {
            val caretLine = document.getLineNumber(offset)
            val problems = mutableListOf<Problem>()
            DaemonCodeAnalyzerImpl.processHighlights(
                document, project, HighlightSeverity.WARNING, 0, document.textLength
            ) { info ->
                if (document.getLineNumber(info.startOffset) == caretLine) {
                    problems.add(toProblem(info, document))
                }
                true
            }
            dedup(problems)
        }

    private fun dedup(problems: List<Problem>): List<Problem> =
        problems
            .sortedWith(compareBy({ it.line }, { it.column }))
            .distinctBy { it.line to it.description }

    private fun toProblem(info: HighlightInfo, document: Document): Problem {
        val line = document.getLineNumber(info.startOffset)
        val col = info.startOffset - document.getLineStartOffset(line)
        val codeLine = document.getText(
            com.intellij.openapi.util.TextRange(
                document.getLineStartOffset(line),
                document.getLineEndOffset(line),
            )
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
