package com.ozd0.context2prompt.format

import com.ozd0.context2prompt.core.Problem

object ProblemFormatter {

    fun format(relativePath: String, problems: List<Problem>): String = buildString {
        append("<problems path=\"").append(relativePath).append("\">\n")
        for (p in problems) {
            append("<problem severity=\"").append(p.severity)
                .append("\" line=\"").append(p.line)
                .append("\" column=\"").append(p.column).append("\">\n")
            append(p.description).append('\n')
            append("> ").append(p.codeLine).append('\n')
            append("</problem>\n")
        }
        append("</problems>\n")
    }
}
