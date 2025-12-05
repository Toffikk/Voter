package io.github.toffikk.voter.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "votes")
@IdClass(Vote.VoteId::class)
data class Vote(
    @Id
    @Column(name = "voter_id")
    val voterId: String = "",

    @Id
    @Column(name = "session_id")
    val sessionId: Int = 0,

    @Column(name = "choice")
    val choice: String = "",

    @Column(name = "created_at")
    val createdAt: Long = Instant.now().toEpochMilli()
) {
    data class VoteId(
        var voterId: String = "",
        var sessionId: Int = 0
    ) : Serializable
}
