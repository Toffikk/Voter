package io.github.toffikk.voter.voting.dto

data class StatusResponse(
    val active: Boolean,
    val endsAt: Long,
    val duration: Int,
    val results: Map<String, Int>?,
    val userVoted: Boolean,
    val voterId: String? = null
)