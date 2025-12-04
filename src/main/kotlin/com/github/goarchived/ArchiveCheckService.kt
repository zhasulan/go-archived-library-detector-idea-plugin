package com.github.goarchived

import com.github.goarchived.settings.PluginSettings
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class RepositoryStatus(
    val isArchived: Boolean,
    val archivedAt: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val description: String? = null
)

@Service(Service.Level.PROJECT)
class ArchiveCheckService(private val project: Project) {
    private val cache = ConcurrentHashMap<String, RepositoryStatus>()
    private val gson = Gson()
    private val cacheValidityMs = TimeUnit.HOURS.toMillis(6) // Кэш на 6 часов

    fun checkRepository(importPath: String): RepositoryStatus? {
        // Проверяем кэш
        val cached = cache[importPath]
        if (cached != null && (System.currentTimeMillis() - cached.lastChecked) < cacheValidityMs) {
            return cached
        }

        // Парсим путь импорта
        val repoInfo = parseImportPath(importPath) ?: return null

        // Проверяем через API
        val status = checkGitHubRepository(repoInfo.owner, repoInfo.repo)
        if (status != null) {
            cache[importPath] = status
        }

        return status
    }

    private data class RepoInfo(val owner: String, val repo: String)

    private fun parseImportPath(importPath: String): RepoInfo? {
        // github.com/user/repo или github.com/user/repo/subpackage
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

            // Опционально: добавьте токен для увеличения лимита запросов
            val token = PluginSettings.getInstance(project).githubToken
            if (token.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }

            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
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
            } else {
                null
            }
        } catch (e: Exception) {
            // Логируем ошибку, но не показываем пользователю
            null
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
