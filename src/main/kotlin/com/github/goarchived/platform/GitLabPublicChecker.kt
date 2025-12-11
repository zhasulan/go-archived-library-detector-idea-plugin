package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.HttpURLConnection
import java.net.URI

/**
 * Checker for public GitLab repositories (gitlab.com)
 * Does not require authentication
 */
open class GitLabPublicChecker(
    private val host: String = "https://gitlab.com",
    private val staleYearsThreshold: Long = 10L
) : RepositoryChecker {
    private val gson = Gson()

    override fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus? {
        return try {
            val projectPath = "$owner/$repo".replace("/", "%2F")
            val uri = URI.create("$host/api/v4/projects/$projectPath")
            val connection = uri.toURL().openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            // Token is optional for public repos
            if (!token.isNullOrEmpty()) {
                connection.setRequestProperty("PRIVATE-TOKEN", token)
            }

            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            // Обработка HTTP кодов
            when (connection.responseCode) {
                200 -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = gson.fromJson(response, JsonObject::class.java)

                    val isArchived = json.get("archived")?.asBoolean ?: false
                    val lastActivityAt = json.get("last_activity_at")?.asString
                    val isStale = RepositoryChecker.isStale(lastActivityAt, staleYearsThreshold)
                    val description = json.get("description")?.asString

                    // Извлекаем звезды и visibility
                    val stars = json.get("star_count")?.asInt
                    val visibility = json.get("visibility")?.asString

                    RepositoryStatus(
                        isArchived = isArchived,
                        archivedAt = if (isArchived) lastActivityAt else null,
                        lastCommitDate = lastActivityAt,
                        description = description,
                        isStale = isStale,
                        stars = stars,
                        visibility = visibility,
                        error = null
                    )
                }
                401, 403 -> throw SecurityException("No access")
                404 -> throw java.io.FileNotFoundException("Project not found")
                else -> throw java.io.IOException("HTTP ${connection.responseCode}: ${connection.responseMessage}")
            }
        } catch (e: java.io.FileNotFoundException) {
            RepositoryStatus(isArchived = false, isStale = false, error = "Project not found")
        } catch (e: SecurityException) {
            RepositoryStatus(isArchived = false, isStale = false, error = "No access")
        } catch (e: java.net.UnknownHostException) {
            RepositoryStatus(isArchived = false, isStale = false, error = "Network error: Unable to reach server")
        } catch (e: java.net.SocketTimeoutException) {
            RepositoryStatus(isArchived = false, isStale = false, error = "Network error: Connection timeout")
        } catch (e: java.io.IOException) {
            RepositoryStatus(isArchived = false, isStale = false, error = "Network error: ${e.message}")
        } catch (e: Exception) {
            RepositoryStatus(isArchived = false, isStale = false, error = "Error: ${e.message}")
        }
    }
}
