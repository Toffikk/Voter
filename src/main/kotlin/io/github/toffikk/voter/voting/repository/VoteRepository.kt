package io.github.toffikk.voter.voting.repository

import io.github.toffikk.voter.model.Vote
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteRepository : CrudRepository<Vote, Vote.VoteId> {
    fun existsByVoterIdAndSessionId(voterId: String, sessionId: Int): Boolean

    @Query("SELECT v.choice, COUNT(v) FROM Vote v WHERE v.sessionId = :sessionId GROUP BY v.choice")
    fun countGroupedByChoiceForSession(sessionId: Int): List<Array<Any>>
}
