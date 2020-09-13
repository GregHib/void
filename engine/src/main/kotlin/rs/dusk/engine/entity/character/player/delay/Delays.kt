package rs.dusk.engine.entity.character.player.delay

import rs.dusk.engine.GameLoop
import rs.dusk.engine.entity.character.player.Player
import kotlin.math.max

class Delays {

    private val delays = mutableMapOf<Delay, Long>()

    fun delayed(delay: Delay): Boolean {
        val delayed = isDelayed(delay)
        if (!delayed) {
            start(delay)
        }
        return delayed
    }

    fun isDelayed(delay: Delay) = delays.getOrDefault(delay, 0) >= GameLoop.tick

    fun start(delay: Delay) {
        delays[delay] = GameLoop.tick + delay.ticks
    }

    fun reset(delay: Delay) {
        delays.remove(delay)
    }

    fun remaining(delay: Delay): Long {
        val tick = delays.getOrDefault(delay, GameLoop.tick)
        return max(0, tick - GameLoop.tick)
    }

    fun elapsed(delay: Delay): Long {
        return delay.ticks - remaining(delay)
    }
}

fun Player.isDelayed(delay: Delay) = delays.isDelayed(delay)

fun Player.delayed(delay: Delay) = delays.delayed(delay)

fun Player.start(delay: Delay) = delays.start(delay)

fun Player.reset(delay: Delay) = delays.reset(delay)

fun Player.elapsed(delay: Delay) = delays.elapsed(delay)

fun Player.remaining(delay: Delay) = delays.remaining(delay)