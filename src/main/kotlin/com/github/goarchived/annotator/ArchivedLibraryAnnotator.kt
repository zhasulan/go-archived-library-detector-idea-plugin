package com.github.goarchived.annotator

import com.github.goarchived.service.ArchiveCheckService
import com.github.goarchived.settings.PluginSettings
import com.github.goarchived.util.GoArchivedBundle
import com.goide.psi.GoImportSpec
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement

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

        // Async check to not block the editor
        ApplicationManager.getApplication().executeOnPooledThread {
            val service = element.project.service<ArchiveCheckService>()
            val status = service.checkRepository(importString)

            if (status?.isArchived == true) {
                ApplicationManager.getApplication().invokeLater {
                    if (element.isValid) {
                        val message = if (status.archivedAt != null) {
                            GoArchivedBundle.message(
                                "message.library.archived.since",
                                importString,
                                status.archivedAt
                            )
                        } else {
                            GoArchivedBundle.message("message.library.archived", importString)
                        }

                        holder.newAnnotation(HighlightSeverity.WARNING, message)
                            .range(element.textRange)
                            .create()
                    }
                }
            }
        }
    }
}
