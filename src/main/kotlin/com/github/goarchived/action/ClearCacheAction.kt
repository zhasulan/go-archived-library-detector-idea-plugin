package com.github.goarchived.action

import com.github.goarchived.service.ArchiveCheckService
import com.github.goarchived.service.LocalCacheService
import com.github.goarchived.util.GoArchivedBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ClearCacheAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        project.service<ArchiveCheckService>().clearCache()
        project.service<LocalCacheService>().clearCache()

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Archived Detector")
            .createNotification(
                GoArchivedBundle.message("message.cache.cleared"),
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = GoArchivedBundle.message("action.clear.cache.text")
        e.presentation.description = GoArchivedBundle.message("action.clear.cache.description")
    }
}
