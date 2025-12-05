package io.github.toffikk.voter.voting.dto

import jakarta.validation.constraints.NotBlank

data class VoteRequest(
    @field:NotBlank(message = "Kategoria głosu nie może być pusta.")
    val vote: String
)