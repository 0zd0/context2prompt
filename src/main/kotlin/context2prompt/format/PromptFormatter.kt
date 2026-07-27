package com.ozd0.context2prompt.format

data class FileEntry(
    val relativePath: String,
    val content: String,
)

object PromptFormatter {

    private const val BASE_TAG = "document"

    fun format(files: List<FileEntry>, includeTree: Boolean, includeContent: Boolean): String {
        val sb = StringBuilder()
        if (includeTree) {
            sb.append("<project_structure>\n")
            sb.append(TreeFormatter.format(files.map { it.relativePath }).text)
            sb.append("\n</project_structure>\n\n")
        }
        if (includeContent) {
            val tag = uniqueTag(files)
            sb.append("<documents>\n")
            for (file in files) {
                sb.append("<").append(tag).append(" path=\"").append(file.relativePath).append("\">\n")
                sb.append(file.content)
                if (!file.content.endsWith('\n')) sb.append('\n')
                sb.append("</").append(tag).append(">\n")
            }
            sb.append("</documents>\n")
        }
        return sb.toString().trimEnd() + "\n"
    }

    /**
     * Base tag; if any file content contains its closing tag,
     * a numeric suffix is added until there are no collisions.
     */
    private fun uniqueTag(files: List<FileEntry>): String {
        var tag = BASE_TAG
        var suffix = 0
        while (files.any { it.content.contains("</$tag>") }) {
            suffix++
            tag = "$BASE_TAG-$suffix"
        }
        return tag
    }
}
