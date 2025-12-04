package com.github.goarchived.service

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ArchiveCheckServiceTest : BasePlatformTestCase() {

    private lateinit var service: ArchiveCheckService

    override fun setUp() {
        super.setUp()
        service = project.getService(ArchiveCheckService::class.java)
    }

    fun testParseGitHubImportPath() {
        // This would require making parseImportPath public or using reflection
        // For now, test through checkRepository
        val result = service.checkRepository("github.com/golang/go")
        assertNotNull(result)
    }

    fun testCheckValidRepository() {
        val result = service.checkRepository("github.com/golang/go")
        assertNotNull(result)
        assertFalse(result!!.isArchived) // golang/go should not be archived
    }

    fun testCheckInvalidPath() {
        val result = service.checkRepository("invalid-path")
        assertNull(result)
    }

    fun testCheckStandardLibrary() {
        val result = service.checkRepository("fmt")
        assertNull(result) // Should not check standard library
    }

    fun testCaching() {
        val path = "github.com/test/repo"

        // First check
        val result1 = service.checkRepository(path)

        // Second check should use cache
        val result2 = service.checkRepository(path)

        // Both should return same result
        assertEquals(result1?.isArchived, result2?.isArchived)
    }

    fun testBatchChecking() {
        val imports = listOf(
            "github.com/golang/go",
            "github.com/kubernetes/kubernetes"
        )

        val results = service.checkRepositoryBatch(imports)

        assertEquals(imports.size, results.size)
        results.forEach { (path, status) ->
            assertNotNull(status)
        }
    }
}
