package com.github.goarchived.action

import com.github.goarchived.service.ArchiveCheckService
import com.github.goarchived.util.GoArchivedBundle
import com.goide.GoFileType
import com.goide.psi.GoFile
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

class CheckAllImportsAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            GoArchivedBundle.message("message.checking.imports"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                val service = project.service<ArchiveCheckService>()
                val psiManager = PsiManager.getInstance(project)

                // Get all Go files with read access
                val goFiles = ReadAction.compute<Collection<com.intellij.openapi.vfs.VirtualFile>, Throwable> {
                    FileTypeIndex.getFiles(
                        GoFileType.INSTANCE,
                        GlobalSearchScope.projectScope(project)
                    )
                }

                indicator.isIndeterminate = false
                val archivedLibraries = mutableSetOf<String>()
                val staleLibraries = mutableSetOf<String>()
                val allImports = mutableSetOf<String>()

                // Collect all imports
                goFiles.forEachIndexed { index, virtualFile ->
                    if (indicator.isCanceled) return

                    indicator.fraction = index.toDouble() / goFiles.size / 2
                    indicator.text = "Scanning ${virtualFile.name}..."

                    ReadAction.run<Throwable> {
                        val psiFile = psiManager.findFile(virtualFile) as? GoFile ?: return@run

                        psiFile.imports.forEach { importSpec ->
                            val importPath = importSpec.path?.replace("\"", "") ?: return@forEach
                            if (importPath.contains(".")) {
                                allImports.add(importPath)
                            }
                        }
                    }
                }

                // Check imports in batch
                indicator.text = "Checking repositories..."
                val results = service.checkRepositoryBatch(allImports.toList())

                results.forEach { (path, status) ->
                    if (status.isArchived) {
                        archivedLibraries.add(path)
                    } else if (status.isStale) {
                        staleLibraries.add(path)
                    }
                }

                // Show results
                showResults(project, archivedLibraries, staleLibraries)
            }
        })
    }

    private fun showResults(project: Project, archived: Set<String>, stale: Set<String>) {
        val totalIssues = archived.size + stale.size

        if (totalIssues == 0) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Go Archived Detector")
                .createNotification(
                    GoArchivedBundle.message("message.no.archived.found"),
                    NotificationType.INFORMATION
                )
                .notify(project)
            return
        }

        val settings = com.github.goarchived.settings.PluginSettings.getInstance(project)
        val service = project.service<ArchiveCheckService>()

        val message = buildString {
            append(GoArchivedBundle.message("message.found.archived", totalIssues))
            append("\n\n")

            if (archived.isNotEmpty()) {
                append("⚠️ Archived (${archived.size}):\n")
                archived.forEach { path ->
                    append("  • $path")

                    // Добавить звезды, если включено
                    if (settings.showStarsCount) {
                        service.checkRepository(path)?.stars?.let { stars ->
                            append(" (⭐ $stars)")
                        }
                    }
                    append("\n")
                }
                append("\n")
            }

            if (stale.isNotEmpty()) {
                append("⏰ Stale (${stale.size}):\n")
                stale.forEach { path ->
                    append("  • $path")

                    // Добавить звезды, если включено
                    if (settings.showStarsCount) {
                        service.checkRepository(path)?.stars?.let { stars ->
                            append(" (⭐ $stars)")
                        }
                    }
                    append("\n")
                }
            }
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Archived Detector")
            .createNotification(
                GoArchivedBundle.message("notification.update.available.title"),
                message,
                NotificationType.WARNING
            )
            .notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.text = GoArchivedBundle.message("action.check.all.imports.text")
        e.presentation.description = GoArchivedBundle.message("action.check.all.imports.description")
    }
}
