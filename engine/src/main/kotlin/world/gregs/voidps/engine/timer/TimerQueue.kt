package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import java.util.*

class TimerQueue(
    private val events: EventDispatcher,
) : Timers {

    val queue = PriorityQueue<Timer>()
    val names = mutableSetOf<String>()

    override fun start(name: String, restart: Boolean): Boolean {
        if (names.contains(name)) {
            return false
        }
        val interval = when (events) {
            is Player -> TimerApi.start(events, name, restart)
            is World -> TimerApi.start(name)
            else -> return false
        }
        if (interval == TimerApi.CANCEL || interval == TimerApi.REPEAT) {
            return false
        }
        val timer = Timer(name, interval)
        queue.add(timer)
        names.add(name)
        return true
    }

    override fun contains(name: String): Boolean = names.contains(name)

    override fun run() {
        val iterator = queue.iterator()
        var timer: Timer
        while (iterator.hasNext()) {
            timer = iterator.next()
            if (!timer.ready()) {
                break
            }
            timer.reset()
            val interval = when (events) {
                is Player -> TimerApi.tick(events, timer.name)
                is World -> TimerApi.tick(timer.name)
                else -> return
            }
            when (interval) {
                TimerApi.CANCEL -> {
                    iterator.remove()
                    names.remove(timer.name)
                    stop(timer.name, logout = false)
                }
                TimerApi.REPEAT -> timer.next()
                else -> timer.next(interval)
            }
        }
    }

    private fun stop(name: String, logout: Boolean) {
        when (events) {
            is Player -> TimerApi.stop(events, name, logout)
            is World -> TimerApi.stop(name, logout)
            else -> return
        }
    }

    override fun stop(name: String) {
        if (clear(name)) {
            stop(name, false)
        }
    }

    override fun clear(name: String): Boolean = names.remove(name) && queue.removeIf { it.name == name }

    override fun clearAll() {
        names.clear()
        queue.clear()
    }

    override fun stopAll() {
        val names = names.toList()
        clearAll()
        for (name in names) {
            stop(name, logout = true)
        }
    }
}
