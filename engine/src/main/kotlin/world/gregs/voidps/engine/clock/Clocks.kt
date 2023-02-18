package world.gregs.voidps.engine.clock

import world.gregs.voidps.engine.GameLoop

class Clocks(val map: MapDelegate) {
    fun start(name: String, ticks: Int = -1) {
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
        if (tick == -1) {
            return true
        }
        return tick > GameLoop.tick
    }

    fun remaining(name: String): Int {
        val tick = map[name] ?: return -1
        if (tick == -1) {
            return -1
        }
        if (tick <= GameLoop.tick) {
            stop(name)
        }
        return tick - GameLoop.tick
    }

    fun toggle(name: String) {
        if (contains(name)) {
            stop(name)
        } else {
            start(name)
        }
    }
}