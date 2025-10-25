package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import java.util.*

class TimerQueue(
    private val events: EventDispatcher,
) : Timers {

    val queue = PriorityQueue<TimerTask>()
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
        if (interval == Timer.CANCEL || interval == Timer.CONTINUE) {
            return false
        }
        val timer = TimerTask(name, interval)
        queue.add(timer)
        names.add(name)
        return true
    }

    override fun contains(name: String): Boolean = names.contains(name)

    override fun run() {
        var timer: TimerTask
        while (queue.isNotEmpty()) {
            timer = queue.peek()
            if (!timer.ready()) {
                break
            }
            queue.poll()
            val interval = when (events) {
                is Player -> TimerApi.tick(events, timer.name)
                is World -> TimerApi.tick(timer.name)
                else -> return
            }
            when (interval) {
                Timer.CANCEL -> {
                    names.remove(timer.name)
                    stop(timer.name, logout = false)
                    continue
                }
                Timer.CONTINUE -> timer.next()
                else -> timer.next(interval)
            }
            queue.offer(timer)
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
