package com.github.goarchived.service

import com.github.goarchived.platform.PlatformCheckerFactory
import com.github.goarchived.settings.PluginSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.concurrent.TimeUnit

data class RepositoryStatus(
    val isArchived: Boolean,
    val archivedAt: String? = null,
    val lastCommitDate: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val description: String? = null,
    val isStale: Boolean = false, // true if last commit > 10 years ago
    val stars: Int? = null, // количество звезд репозитория
    val visibility: String? = null, // "public" или "private"
    val error: String? = null // описание ошибки (401, 404, timeout и т.д.)
)

@Service(Service.Level.PROJECT)
class ArchiveCheckService(private val project: Project) {
    private val localCache: LocalCacheService by lazy {
        project.service<LocalCacheService>()
    }

    /**
     * Check only the cache without making API calls.
     * Returns cached status if valid, null otherwise.
     */
    fun getCachedStatus(importPath: String): RepositoryStatus? {
        val cached = localCache.getCached(importPath)
        val settings = PluginSettings.getInstance(project)
        val cacheValidityMs = TimeUnit.HOURS.toMillis(settings.cacheDurationHours.toLong())

        if (cached != null &&
            (System.currentTimeMillis() - cached.lastChecked) < cacheValidityMs) {
            return RepositoryStatus(
                isArchived = cached.isArchived,
                archivedAt = cached.archivedAt,
                lastCommitDate = cached.lastCommitDate,
                lastChecked = cached.lastChecked,
                description = cached.description,
                isStale = cached.isStale,
                stars = cached.stars,
                visibility = cached.visibility,
                error = cached.error
            )
        }
        return null
    }

    fun checkRepository(importPath: String): RepositoryStatus? {
        // 1. Проверяем локальный кэш
        val cachedStatus = getCachedStatus(importPath)
        if (cachedStatus != null) {
            return cachedStatus
        }

        // 2. Парсим путь импорта
        val repoInfo = parseImportPath(importPath) ?: return null

        // 3. Получаем чекер для платформы
        val checker = PlatformCheckerFactory.createChecker(
            platform = repoInfo.platform,
            host = repoInfo.host,
            project = project
        ) ?: return null

        // 4. Получаем токен для платформы
        val token = PlatformCheckerFactory.getToken(
            platform = repoInfo.platform,
            host = repoInfo.host,
            project = project
        )

        // 5. Проверяем через API платформы
        val status = checker.checkRepository(repoInfo.owner, repoInfo.repo, token)
        if (status != null) {
            localCache.updateCache(importPath, CachedRepository(
                path = importPath,
                isArchived = status.isArchived,
                archivedAt = status.archivedAt,
                lastCommitDate = status.lastCommitDate,
                lastChecked = status.lastChecked,
                description = status.description,
                isStale = status.isStale,
                stars = status.stars,
                visibility = status.visibility,
                error = status.error
            ))
        }

        return status
    }

    fun checkRepositoryBatch(importPaths: List<String>): Map<String, RepositoryStatus> {
        val results = mutableMapOf<String, RepositoryStatus>()
        val settings = PluginSettings.getInstance(project)

        importPaths.chunked(settings.batchCheckSize).forEach { batch ->
            batch.forEach { path ->
                checkRepository(path)?.let { status ->
                    results[path] = status
                }
                // Небольшая задержка между запросами
                Thread.sleep(100)
            }
        }

        return results
    }

    private data class RepoInfo(
        val platform: String,
        val host: String?,
        val owner: String,
        val repo: String
    )

    private fun parseImportPath(importPath: String): RepoInfo? {
        // GitHub
        val githubPattern = Regex("^github\\.com/([^/]+)/([^/]+)(/.*)?$")
        githubPattern.find(importPath)?.let { match ->
            return RepoInfo(
                platform = "github",
                host = null,
                owner = match.groupValues[1],
                repo = match.groupValues[2]
            )
        }

        // GitLab.com
        val gitlabPattern = Regex("^gitlab\\.com/([^/]+)/([^/]+)(/.*)?$")
        gitlabPattern.find(importPath)?.let { match ->
            return RepoInfo(
                platform = "gitlab",
                host = "https://gitlab.com",
                owner = match.groupValues[1],
                repo = match.groupValues[2]
            )
        }

        // Corporate GitLab
        val corporateGitlabPattern = Regex("^([^/]+)/([^/]+)/([^/]+)(/.*)?$")
        corporateGitlabPattern.find(importPath)?.let { match ->
            val potentialHost = match.groupValues[1]
            if (potentialHost.contains("gitlab")) {
                return RepoInfo(
                    platform = "gitlab",
                    host = "https://$potentialHost",
                    owner = match.groupValues[2],
                    repo = match.groupValues[3]
                )
            }
        }

        // Bitbucket
        val bitbucketPattern = Regex("^bitbucket\\.org/([^/]+)/([^/]+)(/.*)?$")
        bitbucketPattern.find(importPath)?.let { match ->
            return RepoInfo(
                platform = "bitbucket",
                host = null,
                owner = match.groupValues[1],
                repo = match.groupValues[2]
            )
        }

        return null
    }

    fun clearCache() {
        localCache.clearCache()
    }
}
