package world.gregs.voidps.engine.entity

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.event.Event

data class EffectStart(val effect: String, val ticks: Int, val restart: Boolean) : Event
data class EffectStop(val effect: String) : Event

fun Entity.start(effect: String, ticks: Int = -1, persist: Boolean = false, quiet: Boolean = false, restart: Boolean = false) {
    val had = hasEffect(effect)
    if (had) {
        stop(effect, quiet)
    }
    startEffect(effect, ticks, persist, had && quiet, restart)
}

private fun Entity.startEffect(effect: String, ticks: Int, persist: Boolean, quiet: Boolean, restart: Boolean) {
    this["${effect}_effect", persist] = ticks
    if (ticks >= 0) {
        this["${effect}_tick"] = GameLoop.tick + ticks
        this["${effect}_job"] = delay(this, ticks) {
            stop(effect)
        }
    }
    if (!quiet) {
        events.emit(EffectStart(effect, ticks, restart))
    }
}

fun Entity.stop(effect: String, quiet: Boolean = false) {
    val stopped = clear("${effect}_effect")
    clear("${effect}_tick")
    remove<Job>("${effect}_job")?.cancel()
    if (stopped && !quiet) {
        events.emit(EffectStop(effect))
    }
}

fun Entity.stopAllEffects(quiet: Boolean = false) {
    values.keys().filter { it.endsWith("_effect") }.forEach {
        stop(it.removeSuffix("_effect"), quiet)
    }
}

fun Entity.hasEffect(effect: String): Boolean = contains("${effect}_effect")

fun Entity.hasOrStart(effect: String, ticks: Int = -1, persist: Boolean = true): Boolean {
    if (hasEffect(effect)) {
        return true
    }
    start(effect, ticks, persist)
    return false
}

fun Entity.remaining(effect: String): Long {
    val expected: Long = getOrNull("${effect}_tick") ?: return -1
    return expected - GameLoop.tick
}

fun Entity.elapsed(effect: String): Long {
    val remaining = remaining(effect)
    if (remaining == -1L) {
        return -1
    }
    val total: Int = getOrNull("${effect}_effect") ?: return -1
    return total - remaining
}

fun Entity.save(effect: String) {
    this[effect] = remaining(effect)
}

fun Entity.restart(effect: String) {
    val ticks: Int = getOrNull("${effect}_effect") ?: return
    startEffect(effect, ticks, persist = true, quiet = false, restart = true)
}

fun Entity.toggle(effect: String, persist: Boolean = false) {
    if (hasEffect(effect)) {
        stop(effect)
    } else {
        start(effect, persist = persist)
    }
}