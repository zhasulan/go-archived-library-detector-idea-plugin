package com.github.goarchived.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "GoArchivedLibraryDetectorSettings",
    storages = [Storage("GoArchivedLibraryDetector.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var githubToken: String = "",
        var checkOnFileOpen: Boolean = true,
        var showInlineWarnings: Boolean = true,
        var cacheDurationHours: Int = 24,
        var batchCheckSize: Int = 50,
        var enableBackgroundUpdates: Boolean = true,
        var updateIntervalHours: Int = 12
    )

    private var myState = State()

    override fun getState(): State = myState
    override fun loadState(state: State) { myState = state }

    var githubToken: String
        get() = myState.githubToken
        set(value) { myState.githubToken = value }

    var checkOnFileOpen: Boolean
        get() = myState.checkOnFileOpen
        set(value) { myState.checkOnFileOpen = value }

    var showInlineWarnings: Boolean
        get() = myState.showInlineWarnings
        set(value) { myState.showInlineWarnings = value }

    var cacheDurationHours: Int
        get() = myState.cacheDurationHours
        set(value) { myState.cacheDurationHours = value }

    var batchCheckSize: Int
        get() = myState.batchCheckSize
        set(value) { myState.batchCheckSize = value }

    var enableBackgroundUpdates: Boolean
        get() = myState.enableBackgroundUpdates
        set(value) { myState.enableBackgroundUpdates = value }

    var updateIntervalHours: Int
        get() = myState.updateIntervalHours
        set(value) { myState.updateIntervalHours = value }

    companion object {
        fun getInstance(project: Project): PluginSettings =
            project.service<PluginSettings>()
    }
}
