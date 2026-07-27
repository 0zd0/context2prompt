package com.ozd0.context2prompt.format

data class FileEntry(
    val relativePath: String,
    val content: String,
)

object PromptFormatter {

    fun format(files: List<FileEntry>, includeTree: Boolean, includeContent: Boolean): String {
        val sb = StringBuilder()
        if (includeTree) {
            sb.append("### Project structure\n")
            sb.append(TreeFormatter.format(files.map { it.relativePath }))
            sb.append('\n')
        }
        if (includeContent) {
            for (file in files) {
                sb.append("### File: ").append(file.relativePath).append('\n')
                sb.append("```").append(extension(file.relativePath)).append('\n')
                sb.append(file.content)
                if (!file.content.endsWith('\n')) sb.append('\n')
                sb.append("```\n\n")
            }
        }
        return sb.toString().trimEnd() + "\n"
    }

    private fun extension(path: String): String {
        val name = path.substringAfterLast('/')
        val ext = name.substringAfterLast('.', missingDelimiterValue = "")
        return ext
    }
}
