package com.github.goarchived.platform

import com.github.goarchived.settings.PluginSettings
import com.intellij.openapi.project.Project

/**
 * Factory for creating appropriate platform checkers
 */
object PlatformCheckerFactory {

    fun createChecker(
        platform: String,
        host: String?,
        project: Project
    ): RepositoryChecker? {
        val settings = PluginSettings.getInstance(project)
        val staleYearsThreshold = settings.staleYearsThreshold.toLong()

        return when (platform) {
            "github" -> GitHubChecker(staleYearsThreshold)

            "gitlab" -> {
                val gitlabHost = host ?: "https://gitlab.com"
                when {
                    gitlabHost == "https://gitlab.com" -> {
                        // Public GitLab.com
                        if (settings.usePrivateRepos && settings.getGitLabToken("gitlab.com").isNotEmpty()) {
                            GitLabPrivateChecker(gitlabHost, staleYearsThreshold)
                        } else {
                            GitLabPublicChecker(gitlabHost, staleYearsThreshold)
                        }
                    }
                    else -> {
                        // Corporate GitLab
                        GitLabCorporateChecker(gitlabHost, staleYearsThreshold)
                    }
                }
            }

            "bitbucket" -> {
                if (settings.usePrivateRepos && settings.bitbucketToken.isNotEmpty()) {
                    BitbucketPrivateChecker(staleYearsThreshold)
                } else {
                    BitbucketPublicChecker(staleYearsThreshold)
                }
            }

            else -> null
        }
    }

    fun getToken(
        platform: String,
        host: String?,
        project: Project
    ): String? {
        val settings = PluginSettings.getInstance(project)

        return when (platform) {
            "github" -> if (settings.usePrivateRepos) settings.githubToken else null
            "gitlab" -> {
                val gitlabHost = host ?: "https://gitlab.com"
                if (settings.usePrivateRepos) {
                    settings.getGitLabToken(gitlabHost)
                } else {
                    null
                }
            }
            "bitbucket" -> if (settings.usePrivateRepos) settings.bitbucketToken else null
            else -> null
        }
    }
}