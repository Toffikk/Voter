package io.github.toffikk.voter.controller

import io.github.toffikk.voter.service.VoteService
import io.github.toffikk.voter.voting.dto.StartRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(private val voteService: VoteService) {

    @PostMapping("/start")
    fun start(@RequestBody req: StartRequest): ResponseEntity<Any> {
        println("vote started")
        val duration = req.duration ?: 30
        val started = voteService.startVoting(duration)
        return if (started) ResponseEntity.ok(mapOf("ok" to true))
        else ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Session already running"))
    }

    @PostMapping("/stop")
    fun stop(): ResponseEntity<Any> {
        println("vote ended")
        voteService.stopVoting()
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    @GetMapping("/status")
    fun status(): ResponseEntity<Any> {
        val session = voteService.getSession()
        return ResponseEntity.ok(
            mapOf(
                "active" to session.active,
                "endsAt" to session.endsAt,
                "results" to session.results.toMap()
            )
        )
    }
}