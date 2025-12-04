package io.github.toffikk.voter.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SchedulerService(private val voteService: VoteService) {

    @Scheduled(fixedRate = 1000)
    fun checkEnd() {
        val session = voteService.getSession()
        if (session.active && Instant.now().toEpochMilli() >= session.endsAt) {
            voteService.stopVoting()
        }
    }
}