package com.github.goarchived.platform

import com.github.goarchived.service.RepositoryStatus
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Base interface for all platform checkers
 */
interface RepositoryChecker {
    fun checkRepository(owner: String, repo: String, token: String?): RepositoryStatus?

    companion object {
        fun isStale(lastCommitDate: String?, staleYearsThreshold: Long = 10L): Boolean {
            return lastCommitDate?.let { date ->
                try {
                    val commitTime = Instant.parse(date)
                    val yearsSince = ChronoUnit.YEARS.between(commitTime, Instant.now())
                    yearsSince >= staleYearsThreshold
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }
}
