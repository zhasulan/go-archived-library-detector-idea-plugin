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
    private val githubTokenField = JBPasswordField()
    private val bitbucketTokenField = JBPasswordField()
    private val usePrivateReposCheckbox = JBCheckBox()
    private val checkOnOpenCheckbox = JBCheckBox()
    private val showInlineCheckbox = JBCheckBox()
    private val cacheDurationField = JBTextField()
    private val batchSizeField = JBTextField()
    private val staleYearsField = JBTextField()
    private val backgroundUpdatesCheckbox = JBCheckBox()
    private val updateIntervalField = JBTextField()
    private val showStarsCheckbox = JBCheckBox()

    override fun getDisplayName(): String {
        return GoArchivedBundle.message("settings.display.name")
    }

    override fun createComponent(): JComponent {
        val settings = PluginSettings.getInstance(project)

        githubTokenField.text = settings.githubToken
        bitbucketTokenField.text = settings.bitbucketToken

        usePrivateReposCheckbox.isSelected = settings.usePrivateRepos
        usePrivateReposCheckbox.text = GoArchivedBundle.message("settings.use.private.repos.label")

        checkOnOpenCheckbox.isSelected = settings.checkOnFileOpen
        checkOnOpenCheckbox.text = GoArchivedBundle.message("settings.check.on.open.label")

        showInlineCheckbox.isSelected = settings.showInlineWarnings
        showInlineCheckbox.text = GoArchivedBundle.message("settings.show.inline.warnings.label")

        cacheDurationField.text = settings.cacheDurationHours.toString()
        batchSizeField.text = settings.batchCheckSize.toString()
        staleYearsField.text = settings.staleYearsThreshold.toString()

        backgroundUpdatesCheckbox.isSelected = settings.enableBackgroundUpdates
        backgroundUpdatesCheckbox.text = GoArchivedBundle.message("settings.background.update.label")

        updateIntervalField.text = settings.updateIntervalHours.toString()

        showStarsCheckbox.isSelected = settings.showStarsCount
        showStarsCheckbox.text = GoArchivedBundle.message("settings.show.stars.label")

        return FormBuilder.createFormBuilder()
            .addComponent(usePrivateReposCheckbox)
            .addSeparator()
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.github.token.label")),
                githubTokenField, 1, false
            )
            .addComponent(JBLabel("<html><small>" +
                    GoArchivedBundle.message("settings.github.token.comment") +
                    "</small></html>"))
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.bitbucket.token.label")),
                bitbucketTokenField, 1, false
            )
            .addComponent(JBLabel("<html><small>" +
                    GoArchivedBundle.message("settings.bitbucket.token.comment") +
                    "</small></html>"))
            .addSeparator()
            .addComponent(checkOnOpenCheckbox)
            .addComponent(showInlineCheckbox)
            .addComponent(showStarsCheckbox)
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.cache.duration.label")),
                cacheDurationField, 1, false
            )
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.batch.size.label")),
                batchSizeField, 1, false
            )
            .addLabeledComponent(
                JBLabel(GoArchivedBundle.message("settings.stale.years.label")),
                staleYearsField, 1, false
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
        return String(githubTokenField.password) != settings.githubToken ||
                String(bitbucketTokenField.password) != settings.bitbucketToken ||
                usePrivateReposCheckbox.isSelected != settings.usePrivateRepos ||
                checkOnOpenCheckbox.isSelected != settings.checkOnFileOpen ||
                showInlineCheckbox.isSelected != settings.showInlineWarnings ||
                showStarsCheckbox.isSelected != settings.showStarsCount ||
                cacheDurationField.text != settings.cacheDurationHours.toString() ||
                batchSizeField.text != settings.batchCheckSize.toString() ||
                staleYearsField.text != settings.staleYearsThreshold.toString() ||
                backgroundUpdatesCheckbox.isSelected != settings.enableBackgroundUpdates ||
                updateIntervalField.text != settings.updateIntervalHours.toString()
    }

    override fun apply() {
        val settings = PluginSettings.getInstance(project)
        settings.githubToken = String(githubTokenField.password)
        settings.bitbucketToken = String(bitbucketTokenField.password)
        settings.usePrivateRepos = usePrivateReposCheckbox.isSelected
        settings.checkOnFileOpen = checkOnOpenCheckbox.isSelected
        settings.showInlineWarnings = showInlineCheckbox.isSelected
        settings.showStarsCount = showStarsCheckbox.isSelected

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

        try {
            settings.staleYearsThreshold = staleYearsField.text.toInt()
        } catch (e: NumberFormatException) {
            settings.staleYearsThreshold = 10
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
