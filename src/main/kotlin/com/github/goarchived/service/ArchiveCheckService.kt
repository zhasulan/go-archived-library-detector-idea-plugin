package com.github.goarchived.service

import com.github.goarchived.settings.PluginSettings
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

data class RepositoryStatus(
    val isArchived: Boolean,
    val archivedAt: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val description: String? = null
)

@Service(Service.Level.PROJECT)
class ArchiveCheckService(private val project: Project) {
    private val gson = Gson()
    private val localCache: LocalCacheService by lazy {
        project.service<LocalCacheService>()
    }

    fun checkRepository(importPath: String): RepositoryStatus? {
        // 1. Проверяем локальный кэш
        val cached = localCache.getCached(importPath)
        val settings = PluginSettings.getInstance(project)
        val cacheValidityMs = TimeUnit.HOURS.toMillis(settings.cacheDurationHours.toLong())

        if (cached != null &&
            (System.currentTimeMillis() - cached.lastChecked) < cacheValidityMs) {
            return RepositoryStatus(
                isArchived = cached.isArchived,
                archivedAt = cached.archivedAt,
                lastChecked = cached.lastChecked,
                description = cached.description
            )
        }

        // 2. Проверяем известные архивированные библиотеки
        if (localCache.isKnownArchived(importPath)) {
            return RepositoryStatus(isArchived = true)
        }

        // 3. Парсим путь импорта
        val repoInfo = parseImportPath(importPath) ?: return null

        // 4. Проверяем через API
        val status = checkGitHubRepository(repoInfo.owner, repoInfo.repo)
        if (status != null) {
            localCache.updateCache(importPath, CachedRepository(
                path = importPath,
                isArchived = status.isArchived,
                archivedAt = status.archivedAt,
                description = status.description
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

    private data class RepoInfo(val owner: String, val repo: String)

    private fun parseImportPath(importPath: String): RepoInfo? {
        val githubPattern = Regex("^github\\.com/([^/]+)/([^/]+)(/.*)?$")
        val match = githubPattern.find(importPath) ?: return null

        return RepoInfo(
            owner = match.groupValues[1],
            repo = match.groupValues[2]
        )
    }

    private fun checkGitHubRepository(owner: String, repo: String): RepositoryStatus? {
        return try {
            val url = URL("https://api.github.com/repos/$owner/$repo")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            val settings = PluginSettings.getInstance(project)
            if (settings.githubToken.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer ${settings.githubToken}")
            }

            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            when (connection.responseCode) {
                200 -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = gson.fromJson(response, JsonObject::class.java)

                    val isArchived = json.get("archived")?.asBoolean ?: false
                    val updatedAt = json.get("updated_at")?.asString
                    val description = json.get("description")?.asString

                    RepositoryStatus(
                        isArchived = isArchived,
                        archivedAt = if (isArchived) updatedAt else null,
                        description = description
                    )
                }
                403 -> {
                    // Rate limit exceeded
                    notifyRateLimit()
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun notifyRateLimit() {
        // TODO: Show notification about rate limit
    }

    fun clearCache() {
        localCache.clearCache()
    }
}
