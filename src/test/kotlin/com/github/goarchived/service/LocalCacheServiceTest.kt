package com.github.goarchived.service

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LocalCacheServiceTest : BasePlatformTestCase() {

    private lateinit var cacheService: LocalCacheService

    override fun setUp() {
        super.setUp()
        cacheService = project.getService(LocalCacheService::class.java)
        cacheService.clearCache()
    }

    fun testSaveAndLoadCache() {
        val testData = mapOf(
            "github.com/test/repo" to CachedRepository(
                path = "github.com/test/repo",
                isArchived = true,
                archivedAt = "2023-01-01"
            )
        )

        cacheService.saveCache(testData)
        val loaded = cacheService.loadCache()

        assertEquals(1, loaded.size)
        assertEquals(true, loaded["github.com/test/repo"]?.isArchived)
    }

    fun testGetCached() {
        val repo = CachedRepository(
            path = "github.com/test/repo",
            isArchived = false
        )

        cacheService.updateCache("github.com/test/repo", repo)
        val cached = cacheService.getCached("github.com/test/repo")

        assertNotNull(cached)
        assertEquals(false, cached?.isArchived)
    }

    fun testClearCache() {
        val repo = CachedRepository(
            path = "github.com/test/repo",
            isArchived = true
        )

        cacheService.updateCache("github.com/test/repo", repo)
        cacheService.clearCache()

        val cache = cacheService.loadCache()
        assertTrue(cache.isEmpty())
    }
}
