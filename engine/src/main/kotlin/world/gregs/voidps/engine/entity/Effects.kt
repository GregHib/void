package world.gregs.voidps.engine.entity

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.delay

fun Entity.start(effect: String, ticks: Int = -1, persist: Boolean = false) {
    values["${effect}_effect", persist] = ticks
    if (ticks >= 0) {
        values["${effect}_tick"] = GameLoop.tick + ticks
        values["${effect}_job"] = delay(this, ticks) {
            stop(effect)
        }
    }
}

fun Entity.stop(effect: String) {
    values.remove("${effect}_effect")
    values.remove("${effect}_tick")
    (values.remove("${effect}_job") as? Job)?.cancel()
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
    val ticks = values["${effect}_effect"] as Int
    start(effect, ticks, true)
}

fun Entity.toggle(effect: String, persist: Boolean = false) {
    if (has(effect)) {
        stop(effect)
    } else {
        start(effect, persist = persist)
    }
}