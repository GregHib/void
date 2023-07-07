package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Events
import java.util.*

class TimerQueue(
    private val events: Events
) : Timers {

    val queue = PriorityQueue<Timer>()
    val names = mutableSetOf<String>()
    private val changes = mutableListOf<Timer>()

    override fun start(name: String, restart: Boolean): Boolean {
        val start = TimerStart(name, restart)
        events.emit(start)
        if (start.cancelled) {
            return false
        }
        val timer = Timer(name, start.interval)
        queue.add(timer)
        names.add(name)
        return true
    }

    override fun contains(name: String): Boolean {
        return names.contains(name)
    }

    override fun run() {
        val iterator = queue.iterator()
        var timer: Timer
        while (iterator.hasNext()) {
            timer = iterator.next()
            if (!timer.ready()) {
                break
            }
            iterator.remove()
            timer.reset()
            val tick = TimerTick(timer.name)
            events.emit(tick)

            if (tick.cancelled) {
                names.remove(timer.name)
                events.emit(TimerStop(timer.name, logout = false))
            } else {
                changes.add(timer)
            }
        }
        if (changes.isNotEmpty()) {
            queue.addAll(changes)
            changes.clear()
        }
    }

    override fun stop(name: String) {
        if (names.remove(name) && queue.removeIf { it.name == name }) {
            events.emit(TimerStop(name, logout = false))
        }
    }

    override fun clearAll() {
        val names = names.toList()
        this.names.clear()
        queue.clear()
        for (name in names) {
            events.emit(TimerStop(name, logout = true))
        }
    }
}