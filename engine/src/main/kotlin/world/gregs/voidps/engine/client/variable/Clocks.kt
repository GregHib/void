package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character

fun Character.start(key: String, duration: Int, base: Int = GameLoop.tick) {
    if (duration == -1) {
        variables.set(key, duration)
    } else {
        variables.set(key, base + duration)
    }
}

fun Character.stop(key: String) {
    variables.clear(key)
}

fun Character.hasClock(key: String, base: Int = GameLoop.tick): Boolean {
    val tick: Int = variables.get(key) ?: return false
    if (tick == -1) {
        return true
    }
    return tick > base
}

fun Character.remaining(key: String, base: Int = GameLoop.tick): Int {
    val tick: Int = variables.get(key) ?: return -1
    if (tick == -1) {
        return -1
    }
    if (tick <= base) {
        stop(key)
        if (tick < base) {
            return -1
        }
    }
    return tick - base
}
