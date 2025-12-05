package io.github.toffikk.voter.controller

import io.github.toffikk.voter.service.VoteResult
import io.github.toffikk.voter.service.VoteService
import io.github.toffikk.voter.voting.repository.VoteRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/vote")
class PublicController(
    private val voteService: VoteService,
    private val voteRepository: VoteRepository
) {

    data class VoteRequest(val category: String)
    data class VoteStatusResponse(
        val active: Boolean,
        val hasVoted: Boolean,
        val results: Map<String, Int>,
        val message: String?,
        val voterId: String,
        val sessionId: Int,
    )

    @GetMapping
    fun getStatus(@CookieValue(value = "voter_id", required = false) voterIdCookie: String?, response: HttpServletResponse): VoteStatusResponse {
        val voterId = voterIdCookie ?: UUID.randomUUID().toString()
        val session = voteService.getSession()
        val hasVoted = voteService.hasVotedThisSession(voterId)

        if (voterIdCookie == null) {
            response.addCookie(Cookie("voter_id", voterId).apply {
                path = "/"
                maxAge = 60 * 60 * 24 * 365
            })
        }
        val message = when {
            session.active -> null
            session.hasEverVoted() -> null
            else -> "Głosowanie jeszcze się nie zaczęło."
        }

        return VoteStatusResponse(
            active = session.active,
            hasVoted = hasVoted,
            results = session.results.toMap(),
            message = message,
            voterId = voterId,
            sessionId = session.id
        )
    }

    @GetMapping("/results/{sessionId}")
    fun getSessionResults(@PathVariable sessionId: Int): VoteStatusResponse {
        val rows = voteRepository.countGroupedByChoiceForSession(sessionId)

        val results = mutableMapOf(
            "ZA" to 0,
            "PRZECIW" to 0,
            "WSTRZYMUJE" to 0
        )

        for (row in rows) {
            val choice = row[0] as String
            val count = (row[1] as Number).toInt()
            results[choice] = count
        }

        return VoteStatusResponse(
            active = false,
            hasVoted = false,
            results = results,
            message = null,
            voterId = "admin",
            sessionId = sessionId
        )
    }

    @PostMapping
    fun vote(@CookieValue("voter_id") voterId: String, @RequestBody body: VoteRequest): ResponseEntity<Any> {
        val category = when (body.category.uppercase()) {
            "ZA", "PRZECIW", "WSTRZYMUJE" -> body.category.uppercase()
            else -> return ResponseEntity.badRequest().body(mapOf("error" to "Invalid vote type"))
        }

        val result = voteService.castVote(voterId, category)

        return when (result) {
            VoteResult.SUCCESS -> ResponseEntity.ok(mapOf("status" to "Vote accepted"))
            VoteResult.ALREADY_VOTED -> ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Already voted"))
            VoteResult.VOTING_CLOSED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Voting is closed"))
        }
    }
}