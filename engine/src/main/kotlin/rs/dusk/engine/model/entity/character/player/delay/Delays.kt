package rs.dusk.engine.model.entity.character.player.delay

import rs.dusk.engine.GameLoop
import rs.dusk.engine.model.entity.character.player.Player

class Delays {

    private val delays = mutableMapOf<Delay, Long>()

    fun delayed(delay: Delay): Boolean {
        val delayed = isDelayed(delay)
        if (!delayed) {
            start(delay)
        }
        return delayed
    }

    fun isDelayed(delay: Delay) = delays.getOrDefault(delay, 0) + delay.ticks >= GameLoop.tick

    fun start(delay: Delay) {
        delays[delay] = GameLoop.tick
    }

    fun reset(delay: Delay) {
        delays.remove(delay)
    }

    fun elapsed(delay: Delay) = GameLoop.tick - delays.getOrDefault(delay, GameLoop.tick)
}

fun Player.isDelayed(delay: Delay) = delays.isDelayed(delay)

fun Player.delayed(delay: Delay) = delays.delayed(delay)

fun Player.start(delay: Delay) = delays.start(delay)

fun Player.reset(delay: Delay) = delays.reset(delay)

fun Player.elapsed(delay: Delay) = delays.elapsed(delay)