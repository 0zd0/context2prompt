package com.ozd0.context2prompt.ui

import com.ozd0.context2prompt.Context2PromptBundle
import com.ozd0.context2prompt.PluginConstants
import com.ozd0.context2prompt.core.SelectedItem
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object PluginUi {

    private const val MAX_LISTED = 10

    fun notifyCopied(project: Project, fileCount: Int, skippedCount: Int, approxTokens: Int) =
        notify(project, Context2PromptBundle.message(
            "notification.copied", fileCount, skippedCount, formatTokens(approxTokens)
        ))

    fun notifyStructureCopied(project: Project, dirCount: Int, fileCount: Int, approxTokens: Int, skippedCount: Int) =
        notify(project, Context2PromptBundle.message(
            "notification.structure.copied", dirCount, fileCount, approxTokens, skippedCount
        ))

    fun notifyProblemsCopied(project: Project, problemCount: Int, approxTokens: Int) =
        notify(project, Context2PromptBundle.message(
            "notification.problems.copied", problemCount, formatTokens(approxTokens)
        ))

    fun notifyNoProblems(project: Project) =
        notify(project, Context2PromptBundle.message("notification.problems.none"))

    private fun notify(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(PluginConstants.NOTIFICATION_GROUP)
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }

    private fun formatTokens(tokens: Int): String = when {
        tokens >= 1_000_000 -> "%.1fM".format(tokens / 1_000_000.0)
        tokens >= 1_000 -> "%.1fk".format(tokens / 1_000.0)
        else -> tokens.toString()
    }


    /**
     * Popup displayed when 'ignored' items are present in the top level of the selection.
     * @return true — include 'ignored' items, false — skip them, null — Cancel/Esc (cancel the action)
     */
    fun askIncludeIgnored(project: Project, ignored: List<SelectedItem<*>>): Boolean? {
        val preview = ignored.take(MAX_LISTED).joinToString("\n") { it.relativePath }
        val more = ignored.size - MAX_LISTED
        val listText = if (more > 0) {
            preview + "\n" + Context2PromptBundle.message("popup.ignored.more", more)
        } else preview

        val result = Messages.showYesNoCancelDialog(
            project,
            Context2PromptBundle.message("popup.ignored.message", ignored.size, listText),
            Context2PromptBundle.message("popup.ignored.title"),
            Context2PromptBundle.message("popup.ignored.include"),
            Context2PromptBundle.message("popup.ignored.skip"),
            Context2PromptBundle.message("popup.ignored.cancel"),
            Messages.getQuestionIcon()
        )
        return when (result) {
            Messages.YES -> true
            Messages.NO -> false
            else -> null
        }
    }
}
