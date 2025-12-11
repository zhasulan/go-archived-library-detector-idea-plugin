package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus

/**
 * Checker for private Bitbucket repositories (bitbucket.org)
 * Requires App Password with Repositories: Read permission
 */
class BitbucketPrivateChecker(staleYearsThreshold: Long = 10L) : BitbucketPublicChecker(staleYearsThreshold) {

    override fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus? {
        if (token.isNullOrEmpty()) {
            throw IllegalArgumentException("Token (App Password) is required for private Bitbucket repositories")
        }

        // Reuse public checker logic with required token
        return super.checkRepository(owner, repo, token)
    }
}
