package com.github.goarchived.settings

import com.github.goarchived.util.GoArchivedBundle
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class PluginSettingsConfigurable(private val project: Project) : Configurable {
    private val tokenField = JBPasswordField()
    private val checkOnOpenCheckbox = JBCheckBox()
    private val showInlineCheckbox = JBCheckBox()
    private val cacheDurationField = JBTextField()
    private val batchSizeField = JBTextField()
    private val backgroundUpdatesCheckbox = JBCheckBox()
    private val updateIntervalField = JBTextField()

    override fun getDisplayName(): String {
        return GoArchivedBundle.message("settings.display.name")
    }

    override fun createComponent(): JComponent {
        val settings = PluginSettings.getInstance(project)

        tokenField.text = settings.githubToken
        checkOnOpenCheckbox.isSelected = settings.checkOnFileOpen
        checkOnOpenCheckbox.text = GoArchivedBundle.message("settings.check.on.open.label")

        showInlineCheckbox.isSelected = settings.showInlineWarnings
        showInlineCheckbox.text = GoArchivedBundle.message("settings.show.inline.warnings.label")

        cacheDurationField.text = settings.cacheDurationHours.toString()
        batchSizeField.text = settings.batchCheckSize.toString()

        backgroundUpdatesCheckbox.isSelected = settings.enableBackgroundUpdates
        backgroundUpdatesCheckbox.text = GoArchivedBundle.message("settings.background.update.label")

        updateIntervalField.text = settings.updateIntervalHours.toString()

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.github.token.label")),
                tokenField, 1, false
            )
            .addComponent(JBLabel("<html><small>" +
                    GoArchivedBundle.message("settings.github.token.comment") +
                    "</small></html>"))
            .addComponent(checkOnOpenCheckbox)
            .addComponent(showInlineCheckbox)
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.cache.duration.label")),
                cacheDurationField, 1, false
            )
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.batch.size.label")),
                batchSizeField, 1, false
            )
            .addComponent(backgroundUpdatesCheckbox)
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.update.interval.label")),
                updateIntervalField, 1, false
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = PluginSettings.getInstance(project)

        val pwd: CharArray = tokenField.password
        val typedToken = String(pwd)
        pwd.fill('\u0000') // безопасное обнуление

        return typedToken != settings.githubToken ||
                checkOnOpenCheckbox.isSelected != settings.checkOnFileOpen ||
                showInlineCheckbox.isSelected != settings.showInlineWarnings ||
                cacheDurationField.text != settings.cacheDurationHours.toString() ||
                batchSizeField.text != settings.batchCheckSize.toString() ||
                backgroundUpdatesCheckbox.isSelected != settings.enableBackgroundUpdates ||
                updateIntervalField.text != settings.updateIntervalHours.toString()
    }

    override fun apply() {
        val settings = PluginSettings.getInstance(project)
        settings.githubToken = String(tokenField.password)
        settings.checkOnFileOpen = checkOnOpenCheckbox.isSelected
        settings.showInlineWarnings = showInlineCheckbox.isSelected

        try {
            settings.cacheDurationHours = cacheDurationField.text.toInt()
        } catch (e: NumberFormatException) {
            settings.cacheDurationHours = 24
        }

        try {
            settings.batchCheckSize = batchSizeField.text.toInt()
        } catch (e: NumberFormatException) {
            settings.batchCheckSize = 50
        }

        settings.enableBackgroundUpdates = backgroundUpdatesCheckbox.isSelected

        try {
            settings.updateIntervalHours = updateIntervalField.text.toInt()
        } catch (e: NumberFormatException) {
            settings.updateIntervalHours = 12
        }

        // Clear cache when settings change
        project.service<com.github.goarchived.service.ArchiveCheckService>().clearCache()
    }
}
