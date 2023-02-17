package world.gregs.voidps.engine.clock

import world.gregs.voidps.engine.GameLoop

class Clocks {
    val map = mutableMapOf<String, Long>()

    fun start(name: String, ticks: Int = -1, persist: Boolean = false) {
        if (ticks == -1) {
            this.map[name] = -1
        } else {
            this.map[name] = GameLoop.tick + ticks
        }
    }

    fun stop(name: String) {
        map.remove(name)
    }

    fun contains(name: String): Boolean {
        val tick = map[name] ?: return false
        if (tick == -1L) {
            return true
        }
        return tick > GameLoop.tick
    }

    fun remaining(name: String): Int {
        val tick = map[name] ?: return -1
        if (tick == -1L) {
            return -1
        }
        if (tick <= GameLoop.tick) {
            stop(name)
        }
        val remaining = tick - GameLoop.tick
        return remaining.toInt()
    }

    fun toggle(name: String, persist: Boolean = false) {
        if (contains(name)) {
            stop(name)
        } else {
            start(name, persist = persist)
        }
    }
}