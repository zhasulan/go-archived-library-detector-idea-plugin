package com.github.goarchived.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "GoArchivedLibraryDetectorSettings",
    storages = [Storage("GoArchivedLibraryDetector.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class GitLabHostConfig(
        var host: String = "",
        var token: String = ""
    )

    data class State(
        var githubToken: String = "",
        var bitbucketToken: String = "",
        var checkOnFileOpen: Boolean = true,
        var showInlineWarnings: Boolean = true,
        var cacheDurationHours: Int = 24,
        var batchCheckSize: Int = 50,
        var enableBackgroundUpdates: Boolean = true,
        var updateIntervalHours: Int = 12,
        var usePrivateRepos: Boolean = false,
        var staleYearsThreshold: Int = 10,
        var gitlabHosts: MutableList<GitLabHostConfig> = mutableListOf(),
        var showStarsCount: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState
    override fun loadState(state: State) { myState = state }

    var githubToken: String
        get() = myState.githubToken
        set(value) { myState.githubToken = value }

    var bitbucketToken: String
        get() = myState.bitbucketToken
        set(value) { myState.bitbucketToken = value }

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

    var usePrivateRepos: Boolean
        get() = myState.usePrivateRepos
        set(value) { myState.usePrivateRepos = value }

    var staleYearsThreshold: Int
        get() = myState.staleYearsThreshold
        set(value) { myState.staleYearsThreshold = value }

    var gitlabHosts: MutableList<GitLabHostConfig>
        get() = myState.gitlabHosts
        set(value) { myState.gitlabHosts = value }

    var showStarsCount: Boolean
        get() = myState.showStarsCount
        set(value) { myState.showStarsCount = value }

    // Helper methods
    val customGitLabHosts: List<String>
        get() = gitlabHosts.map { it.host }

    fun getGitLabToken(host: String): String {
        return gitlabHosts.find { it.host == host }?.token ?: ""
    }

    fun addGitLabHost(host: String, token: String) {
        val existing = gitlabHosts.find { it.host == host }
        if (existing != null) {
            existing.token = token
        } else {
            gitlabHosts.add(GitLabHostConfig(host, token))
        }
    }

    fun removeGitLabHost(host: String) {
        gitlabHosts.removeIf { it.host == host }
    }

    companion object {
        fun getInstance(project: Project): PluginSettings =
            project.service<PluginSettings>()
    }
}
