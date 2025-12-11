package com.github.goarchived.annotator

import com.github.goarchived.service.ArchiveCheckService
import com.github.goarchived.settings.PluginSettings
import com.goide.psi.GoImportSpec
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import java.time.Instant
import java.time.format.DateTimeFormatter

class ArchivedLibraryAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is GoImportSpec) return

        val settings = PluginSettings.getInstance(element.project)
        if (!settings.showInlineWarnings) return

        val importPath = element.path ?: return
        val importString = importPath.replace("\"", "")

        // Skip standard library and relative imports
        if (!importString.contains(".") || importString.startsWith(".")) {
            return
        }

        val service = element.project.service<ArchiveCheckService>()

        // Check cache synchronously - this is fast
        val status = service.getCachedStatus(importString)

        if (status != null) {
            // We have cached data, create annotation immediately
            val message = buildWarningMessage(importString, status, settings)
            val severity = if (status.isArchived) {
                HighlightSeverity.WARNING
            } else if (status.isStale) {
                HighlightSeverity.WEAK_WARNING
            } else {
                return
            }

            // Validate element is still valid before annotating
            if (!element.isValid) return

            // Get text range and validate it
            val range = element.textRange
            if (range == null || range.startOffset < 0 || range.endOffset <= range.startOffset) return

            // Additional check: ensure the range is within the document
            val document = holder.currentAnnotationSession.file.viewProvider.document
            if (document != null && (range.endOffset > document.textLength || range.startOffset >= document.textLength)) {
                return
            }

            holder.newAnnotation(severity, message)
                .range(range)
                .create()
        } else {
            // Cache miss - schedule async check without creating annotation
            // The check will update the cache and trigger re-annotation
            val project = element.project
            val containingFile = element.containingFile

            ApplicationManager.getApplication().executeOnPooledThread {
                // This will update the cache
                service.checkRepository(importString)

                // Trigger re-annotation to show the results
                ApplicationManager.getApplication().invokeLater {
                    if (containingFile.isValid) {
                        DaemonCodeAnalyzer.getInstance(project).restart(containingFile)
                    }
                }
            }
        }
    }

    private fun buildWarningMessage(
        importPath: String,
        status: com.github.goarchived.service.RepositoryStatus,
        settings: PluginSettings
    ): String {
        return buildString {
            // Проверка на ошибку
            if (status.error != null) {
                append("⚠️ Failed to check '$importPath': ${status.error}")
                return@buildString
            }

            if (status.isArchived) {
                append("⚠️ Library '$importPath' has been archived")

                // Показать звезды, если включено
                if (settings.showStarsCount && status.stars != null) {
                    append(" (⭐ ${status.stars} stars)")
                }

                // Show last commit date instead of archived date
                status.lastCommitDate?.let { date ->
                    val formatted = formatDate(date)
                    append(" (last commit: $formatted)")
                }
            } else if (status.isStale) {
                append("⏰ Library '$importPath' appears inactive")

                // Показать звезды, если включено
                if (settings.showStarsCount && status.stars != null) {
                    append(" (⭐ ${status.stars} stars)")
                }

                status.lastCommitDate?.let { date ->
                    val formatted = formatDate(date)
                    val yearsAgo = calculateYearsAgo(date)
                    append(" (last commit: $formatted, ~$yearsAgo years ago)")
                }
            }

            status.description?.let { desc ->
                if (desc.isNotEmpty()) {
                    append("\n📝 ")
                    append(desc)
                }
            }
        }
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val instant = Instant.parse(isoDate)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            formatter.format(instant.atZone(java.time.ZoneId.systemDefault()))
        } catch (e: Exception) {
            isoDate // fallback to original
        }
    }

    private fun calculateYearsAgo(isoDate: String): Long {
        return try {
            val commitTime = Instant.parse(isoDate)
            java.time.temporal.ChronoUnit.YEARS.between(commitTime, Instant.now())
        } catch (e: Exception) {
            0
        }
    }
}
