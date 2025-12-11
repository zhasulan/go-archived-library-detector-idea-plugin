package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus

/**
 * Checker for corporate GitLab instances (e.g., gitlab.company.com)
 * Supports both public and private repositories within the corporate network
 */
class GitLabCorporateChecker(
    private val corporateHost: String,
    staleYearsThreshold: Long = 10L
) : GitLabPublicChecker(corporateHost, staleYearsThreshold) {

    /**
     * Check public corporate repository (no token required if public)
     */
    fun checkPublicRepository(owner: String, repo: String): RepositoryStatus? {
        return super.checkRepository(owner, repo, null)
    }

    /**
     * Check private corporate repository (token required)
     */
    fun checkPrivateRepository(owner: String, repo: String, token: String): RepositoryStatus? {
        if (token.isEmpty()) {
            throw IllegalArgumentException("Token is required for private corporate GitLab repositories")
        }

        return super.checkRepository(owner, repo, token)
    }

    override fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus? {
        // Auto-detect based on token presence
        return if (token.isNullOrEmpty()) {
            checkPublicRepository(owner, repo)
        } else {
            checkPrivateRepository(owner, repo, token)
        }
    }
}
