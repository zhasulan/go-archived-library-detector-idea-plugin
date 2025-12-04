package com.github.goarchived.inspection

import com.github.goarchived.service.ArchiveCheckService
import com.github.goarchived.util.GoArchivedBundle
import com.goide.psi.GoFile
import com.goide.psi.GoImportSpec
import com.intellij.codeInspection.*
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

class ArchivedLibraryInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is GoFile) return

                val service = file.project.service<ArchiveCheckService>()

                file.imports.forEach { importSpec ->
                    val importPath = importSpec.path?.replace("\"", "") ?: return@forEach

                    // Skip standard library
                    if (!importPath.contains(".")) return@forEach

                    val status = service.checkRepository(importPath)
                    if (status?.isArchived == true) {
                        val message = if (status.archivedAt != null) {
                            GoArchivedBundle.message(
                                "message.library.archived.since",
                                importPath,
                                status.archivedAt
                            )
                        } else {
                            GoArchivedBundle.message("message.library.archived", importPath)
                        }

                        holder.registerProblem(
                            importSpec,
                            message,
                            ProblemHighlightType.WARNING,
                            RemoveImportFix(importSpec)
                        )
                    }
                }
            }
        }
    }

    private class RemoveImportFix(private val importSpec: GoImportSpec) :
        LocalQuickFix {

        override fun getName(): String = "Remove archived import"

        override fun getFamilyName(): String = "Remove archived import"

        override fun applyFix(
            project: com.intellij.openapi.project.Project,
            descriptor: ProblemDescriptor
        ) {
            importSpec.delete()
        }
    }
}
