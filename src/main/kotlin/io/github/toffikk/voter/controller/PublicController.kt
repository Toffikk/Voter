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
    fun getStatus(@RequestHeader("X-Voter-Id") voterIdCookie: String, response: HttpServletResponse): VoteStatusResponse {
        val session = voteService.getSession()
        val hasVoted = voteService.hasVotedThisSession(voterIdCookie)

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
            voterId = voterIdCookie,
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
    fun vote(@RequestHeader("X-Voter-Id") voterIdCookie: String, @RequestBody body: VoteRequest, response: HttpServletResponse): ResponseEntity<Any> {
        val category = when (body.category.uppercase()) {
            "ZA", "PRZECIW", "WSTRZYMUJE" -> body.category.uppercase()
            else -> return ResponseEntity.badRequest().body(mapOf("error" to "Invalid vote type"))
        }

        val result = voteService.castVote(voterIdCookie, category)

        return when (result) {
            VoteResult.SUCCESS -> ResponseEntity.ok(mapOf("status" to "Vote accepted"))
            VoteResult.ALREADY_VOTED -> ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Already voted"))
            VoteResult.VOTING_CLOSED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Voting is closed"))
        }
    }
}