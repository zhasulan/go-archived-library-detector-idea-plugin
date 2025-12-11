package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.HttpURLConnection
import java.net.URI

/**
 * Checker for GitHub repositories (github.com)
 * Supports both public and private repositories
 */
class GitHubChecker(private val staleYearsThreshold: Long = 10L) : RepositoryChecker {
    private val gson = Gson()

    override fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus? {
        return try {
            // Get repository info
            val repoInfo = fetchRepositoryInfo(owner, repo, token)

            // Get last commit date
            val defaultBranch = repoInfo.get("default_branch")?.asString ?: "main"
            val lastCommitDate = fetchLastCommitDate(owner, repo, defaultBranch, token)
                ?: repoInfo.get("updated_at")?.asString

            val isArchived = repoInfo.get("archived")?.asBoolean ?: false
            val isStale = RepositoryChecker.isStale(lastCommitDate, staleYearsThreshold)
            val description = repoInfo.get("description")?.asString

            // Извлекаем звезды и visibility
            val stars = repoInfo.get("stargazers_count")?.asInt
            val visibility = if (repoInfo.get("private")?.asBoolean == true) "private" else "public"

            RepositoryStatus(
                isArchived = isArchived,
                archivedAt = if (isArchived) repoInfo.get("updated_at")?.asString else null,
                lastCommitDate = lastCommitDate,
                description = description,
                isStale = isStale,
                stars = stars,
                visibility = visibility,
                error = null
            )
        } catch (e: java.io.FileNotFoundException) {
            RepositoryStatus(isArchived = false, isStale = false, error = "Repository not found")
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

    private fun fetchRepositoryInfo(owner: String, repo: String, token: String?): JsonObject {
        val uri = URI.create("https://api.github.com/repos/$owner/$repo")
        val connection = uri.toURL().openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

        if (!token.isNullOrEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer $token")
        }

        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        when (connection.responseCode) {
            200 -> {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                return gson.fromJson(response, JsonObject::class.java)
            }
            401, 403 -> throw SecurityException("No access")
            404 -> throw java.io.FileNotFoundException("Repository not found")
            else -> throw java.io.IOException("HTTP ${connection.responseCode}: ${connection.responseMessage}")
        }
    }

    private fun fetchLastCommitDate(owner: String, repo: String, branch: String, token: String?): String? {
        return try {
            val uri = URI.create("https://api.github.com/repos/$owner/$repo/commits/$branch")
            val connection = uri.toURL().openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (!token.isNullOrEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }

            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode != 200) {
                return null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = gson.fromJson(response, JsonObject::class.java)

            json.getAsJsonObject("commit")
                ?.getAsJsonObject("committer")
                ?.get("date")?.asString
        } catch (e: Exception) {
            null
        }
    }
}
