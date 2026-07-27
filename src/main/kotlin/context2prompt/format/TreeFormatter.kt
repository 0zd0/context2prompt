package com.ozd0.context2prompt.format

data class TreeResult(
    val text: String,
    val dirCount: Int,
    val fileCount: Int,
)

object TreeFormatter {

    fun format(filePaths: List<String>, dirPaths: List<String> = emptyList()): TreeResult {
        val grouped = filePaths
            .distinct()
            .groupBy(
                keySelector = { it.substringBeforeLast('/', missingDelimiterValue = "") },
                valueTransform = { it.substringAfterLast('/') }
            )

        val emptyDirs = dirPaths
            .distinct()
            .map { it.trimEnd('/') }
            .filter { it.isNotEmpty() && it !in grouped.keys }

        val all = grouped + emptyDirs.associateWith { emptyList() }

        val text = all.toSortedMap().entries.joinToString("\n") { (dir, files) ->
            val prefix = if (dir.isEmpty()) "(root)" else dir
            if (files.isEmpty()) "$prefix: (empty)"
            else "$prefix: ${files.sorted().joinToString(", ")}"
        }

        return TreeResult(text, dirCount = all.size, fileCount = grouped.values.sumOf { it.distinct().size })

    }
}
