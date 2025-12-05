package io.github.toffikk.voter.voting.dto

import jakarta.validation.constraints.Min

data class StartRequest(
    @field:Min(value = 1, message = "Długość sesji musi wynosić co najmniej 1 sekundę.")
    val duration: Int = 0
)