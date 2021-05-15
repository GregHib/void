package world.gregs.voidps.engine.entity

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.event.Event

data class EffectStart(val effect: String) : Event
data class EffectStop(val effect: String) : Event

fun Entity.start(effect: String, ticks: Int = -1, persist: Boolean = false) {
    values["${effect}_effect", persist] = ticks
    if (ticks >= 0) {
        values["${effect}_tick"] = GameLoop.tick + ticks
        values["${effect}_job"] = delay(this, ticks) {
            stop(effect)
        }
    }
    events.emit(EffectStart(effect))
}

fun Entity.stop(effect: String) {
    val stopped = values.remove("${effect}_effect") != null
    values.remove("${effect}_tick")
    (values.remove("${effect}_job") as? Job)?.cancel()
    if (stopped) {
        events.emit(EffectStop(effect))
    }
}

fun Entity.has(effect: String): Boolean {
    return values.containsKey("${effect}_effect")
}

fun Entity.remaining(effect: String): Int {
    val expected = values["${effect}_tick"] as? Long ?: return -1
    return (expected - GameLoop.tick).toInt()
}

fun Entity.save(effect: String) {
    values[effect] = remaining(effect)
}

fun Entity.restart(effect: String) {
    val ticks = values["${effect}_effect"] as? Int ?: return
    start(effect, ticks, true)
}

fun Entity.toggle(effect: String, persist: Boolean = false) {
    if (has(effect)) {
        stop(effect)
    } else {
        start(effect, persist = persist)
    }
}