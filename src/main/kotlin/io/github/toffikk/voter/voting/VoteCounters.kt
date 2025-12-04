package io.github.toffikk.voter.voting

import java.util.concurrent.atomic.AtomicInteger

data class VoteCounters(
    var za: AtomicInteger = AtomicInteger(0),
    var przeciw: AtomicInteger = AtomicInteger(0),
    var wstrzymuje: AtomicInteger = AtomicInteger(0)
) {
    fun reset() {
        za.set(0)
        przeciw.set(0)
        wstrzymuje.set(0)
    }

    fun toMap(): Map<String, Int> = mapOf(
        "ZA" to za.get(),
        "PRZECIW" to przeciw.get(),
        "WSTRZYMUJE" to wstrzymuje.get()
    )
}
