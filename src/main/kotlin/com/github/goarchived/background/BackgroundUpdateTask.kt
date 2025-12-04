package com.github.goarchived.background

import com.github.goarchived.service.ArchiveCheckService
import com.github.goarchived.service.LocalCacheService
import com.github.goarchived.settings.PluginSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class BackgroundUpdateTask(private val project: Project) {
    private val executor = Executors.newSingleThreadScheduledExecutor()

    init {
        scheduleUpdate()
    }

    private fun scheduleUpdate() {
        val settings = PluginSettings.getInstance(project)

        if (!settings.enableBackgroundUpdates) return

        executor.scheduleAtFixedRate(
            { updateCache() },
            settings.updateIntervalHours.toLong(),
            settings.updateIntervalHours.toLong(),
            TimeUnit.HOURS
        )
    }

    private fun updateCache() {
        val localCache = project.service<LocalCacheService>()
        val archiveService = project.service<ArchiveCheckService>()
        val settings = PluginSettings.getInstance(project)

        val cache = localCache.loadCache()
        val expiredEntries = cache.filter { (_, cached) ->
            val ageMs = System.currentTimeMillis() - cached.lastChecked
            ageMs > TimeUnit.HOURS.toMillis(settings.cacheDurationHours.toLong())
        }

        // Update expired entries
        expiredEntries.keys.forEach { path ->
            archiveService.checkRepository(path)
        }
    }

    fun dispose() {
        executor.shutdown()
    }
}