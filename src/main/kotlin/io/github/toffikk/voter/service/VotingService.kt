package io.github.toffikk.voter.service

import io.github.toffikk.voter.model.Vote
import io.github.toffikk.voter.voting.VoteCounters
import io.github.toffikk.voter.voting.repository.VoteRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Collections

@Service
class VoteService(private val voteRepository: VoteRepository) {

    data class VotingSession(
        val id: Int = 0,
        var active: Boolean = false,
        var endsAt: Long = 0,
        var results: VoteCounters = VoteCounters(),
        val votedUsers: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())
    ) {
        fun hasEverVoted() = results.za.get() + results.przeciw.get() + results.wstrzymuje.get() > 0
    }

    private var currentSession = VotingSession(id = 0)

    fun startVoting(durationSeconds: Int): Boolean {
        if (currentSession.active) return false
        currentSession = VotingSession(
            id = currentSession.id + 1,
            active = true,
            endsAt = Instant.now().toEpochMilli() + durationSeconds * 1000
        )
        println("succeeded")
        return true
    }

    fun stopVoting() {
        currentSession.active = false
        println("succeeded")
    }

    fun getSession() = currentSession

    fun hasVotedThisSession(voterId: String) = currentSession.votedUsers.contains(voterId)

    fun castVote(voterId: String, vote: String): VoteResult {
        if (!currentSession.active) return VoteResult.VOTING_CLOSED
        if (hasVotedThisSession(voterId)) return VoteResult.ALREADY_VOTED

        currentSession.votedUsers.add(voterId)

        when (vote.uppercase()) {
            "ZA" -> currentSession.results.za.incrementAndGet()
            "PRZECIW" -> currentSession.results.przeciw.incrementAndGet()
            "WSTRZYMUJE" -> currentSession.results.wstrzymuje.incrementAndGet()
        }
        println("succeeded to cast vote")

        voteRepository.save(Vote(voterId = voterId, sessionId = currentSession.id, choice = vote.uppercase()))

        return VoteResult.SUCCESS
    }
}
