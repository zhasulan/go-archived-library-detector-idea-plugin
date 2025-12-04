package com.github.goarchived.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CachedRepository(
    val path: String,
    val isArchived: Boolean,
    val archivedAt: String? = null,
    val lastChecked: Long = System.currentTimeMillis(),
    val description: String? = null
)

@Service(Service.Level.PROJECT)
class LocalCacheService(private val project: Project) {
    private val gson = Gson()
    private val cacheFile: File by lazy {
        File(project.basePath, ".idea/go-archived-cache.json")
    }

    // Предустановленный список популярных архивированных библиотек
    private val knownArchivedLibraries = mapOf(
        "github.com/go-sql-driver/mysql" to false, // Пример активной
        // Добавьте сюда известные архивированные библиотеки
    )

    fun loadCache(): Map<String, CachedRepository> {
        if (!cacheFile.exists()) {
            return emptyMap()
        }

        return try {
            FileReader(cacheFile).use { reader ->
                val type = object : TypeToken<Map<String, CachedRepository>>() {}.type
                gson.fromJson(reader, type) ?: emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveCache(cache: Map<String, CachedRepository>) {
        try {
            cacheFile.parentFile?.mkdirs()
            FileWriter(cacheFile).use { writer ->
                gson.toJson(cache, writer)
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    fun getCached(importPath: String): CachedRepository? {
        val cache = loadCache()
        return cache[importPath]
    }

    fun updateCache(importPath: String, repo: CachedRepository) {
        val cache = loadCache().toMutableMap()
        cache[importPath] = repo
        saveCache(cache)
    }

    fun clearCache() {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    fun isKnownArchived(importPath: String): Boolean {
        return knownArchivedLibraries[importPath] == true
    }
}