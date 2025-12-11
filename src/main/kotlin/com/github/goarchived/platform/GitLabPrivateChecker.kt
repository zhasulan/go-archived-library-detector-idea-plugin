package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus

/**
 * Checker for private GitLab repositories (gitlab.com)
 * Requires authentication token with read_api scope
 */
class GitLabPrivateChecker(
    host: String = "https://gitlab.com",
    staleYearsThreshold: Long = 10L
) : GitLabPublicChecker(host, staleYearsThreshold) {

    override fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus? {
        if (token.isNullOrEmpty()) {
            throw IllegalArgumentException("Token is required for private GitLab repositories")
        }

        // Reuse public checker logic with required token
        return super.checkRepository(owner, repo, token)
    }
}
