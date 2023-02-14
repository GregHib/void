package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.Job

data class EffectStart(val effect: String, val ticks: Int = -1, val restart: Boolean = false) : Event
data class EffectStop(val effect: String) : Event

/**
 * Starts an effect
 * @param ticks Number of game ticks to last before the effect is removed
 * @param persist whether the effect should be saved after logout
 * @param quiet whether [EffectStart] & [EffectStop] events should be emitted
 * @param restart [EffectStart] value to identify whether an effect should be re-applied
 */
fun Character.start(effect: String, ticks: Int = -1, persist: Boolean = false, quiet: Boolean = false, restart: Boolean = false) {
    val had = hasEffect(effect)
    if (had) {
        stop(effect, quiet)
    }
    startEffect(effect, ticks, persist, had && quiet, restart)
}

private fun Character.startEffect(effect: String, ticks: Int, persist: Boolean, quiet: Boolean, restart: Boolean) {
    this["${effect}_effect", persist] = ticks
    if (ticks >= 0) {
        this["${effect}_tick"] = GameLoop.tick + ticks
        if(this is Player) {
            this["${effect}_job"] = softQueue(ticks) {
                stop(effect)
            }
        } else if(this is NPC) {
            this["${effect}_job"] = softQueue(ticks) {
                stop(effect)
            }
        }
    }
    if (!quiet) {
        events.emit(EffectStart(effect, ticks, restart))
    }
}

fun Character.stop(effect: String, quiet: Boolean = false) {
    val stopped = clear("${effect}_effect")
    clear("${effect}_tick")
    remove<Job>("${effect}_job")?.cancel()
    if (stopped && !quiet) {
        events.emit(EffectStop(effect))
    }
}

fun Character.stopAllEffects(quiet: Boolean = false) {
    values?.keys()?.filter { it.endsWith("_effect") }?.forEach {
        stop(it.removeSuffix("_effect"), quiet)
    }
}

fun Character.hasEffect(effect: String): Boolean = contains("${effect}_effect")

fun Character.hasOrStart(effect: String, ticks: Int = -1, persist: Boolean = false): Boolean {
    if (hasEffect(effect)) {
        return true
    }
    start(effect, ticks, persist)
    return false
}

fun Character.remaining(effect: String): Long {
    val expected: Long = getOrNull("${effect}_tick") ?: return -1
    return expected - GameLoop.tick
}

fun Character.elapsed(effect: String): Long {
    val remaining = remaining(effect)
    if (remaining == -1L) {
        return -1
    }
    val total: Int = getOrNull("${effect}_effect") ?: return -1
    return total - remaining
}

fun Character.save(effect: String) {
    this[effect] = remaining(effect)
}

/**
 * Restart persistent effect count down after re-login
 */
fun Character.restart(effect: String) {
    val ticks: Int = getOrNull("${effect}_effect") ?: return
    startEffect(effect, ticks, persist = true, quiet = false, restart = true)
}

fun Character.toggle(effect: String, persist: Boolean = false) {
    if (hasEffect(effect)) {
        stop(effect)
    } else {
        start(effect, persist = persist)
    }
}