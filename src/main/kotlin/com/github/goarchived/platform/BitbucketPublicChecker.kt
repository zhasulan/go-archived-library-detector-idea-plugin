package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.net.HttpURLConnection
import java.net.URI

/**
 * Checker for public Bitbucket repositories (bitbucket.org)
 * Note: Bitbucket doesn't have an "archived" status, only staleness detection
 */
open class BitbucketPublicChecker(private val staleYearsThreshold: Long = 10L) : RepositoryChecker {
    private val gson = Gson()

    override fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus? {
        return try {
            // Get repository info
            val repoInfo = fetchRepositoryInfo(owner, repo, token)

            // Get last commit
            val lastCommitDate = fetchLastCommitDate(owner, repo, token)
                ?: repoInfo.get("updated_on")?.asString

            val isStale = RepositoryChecker.isStale(lastCommitDate, staleYearsThreshold)
            val description = repoInfo.get("description")?.asString

            // Извлекаем visibility (Bitbucket не имеет прямого поля stars)
            val visibility = if (repoInfo.get("is_private")?.asBoolean == true) "private" else "public"

            RepositoryStatus(
                isArchived = false, // Bitbucket doesn't have archived status
                archivedAt = null,
                lastCommitDate = lastCommitDate,
                description = description,
                isStale = isStale,
                stars = null, // Bitbucket doesn't have a stars field, only watchers
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
        val uri = URI.create("https://api.bitbucket.org/2.0/repositories/$owner/$repo")
        val connection = uri.toURL().openConnection() as HttpURLConnection

        connection.requestMethod = "GET"

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

    private fun fetchLastCommitDate(owner: String, repo: String, token: String?): String? {
        return try {
            val uri = URI.create("https://api.bitbucket.org/2.0/repositories/$owner/$repo/commits")
            val connection = uri.toURL().openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

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

            json.getAsJsonArray("values")
                ?.get(0)?.asJsonObject
                ?.get("date")?.asString
        } catch (e: Exception) {
            null
        }
    }
}
