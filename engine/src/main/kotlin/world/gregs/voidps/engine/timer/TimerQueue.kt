package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Events
import java.util.*

class TimerQueue(
    private val events: Events
) : Timers {

    val queue = PriorityQueue<Timer>()

    override fun start(name: String, restart: Boolean): Boolean {
        val start = TimerStart(name, restart)
        events.emit(start)
        if (start.cancelled) {
            return false
        }
        val timer = Timer(name, start.interval)
        queue.add(timer)
        return true
    }

    override fun contains(name: String): Boolean {
        return queue.any { it.name == name }
    }

    override fun run() {
        val iterator = queue.iterator()
        var timer: Timer
        while (iterator.hasNext()) {
            timer = iterator.next()
            if (!timer.ready()) {
                break
            }
            timer.reset()
            val tick = TimerTick(timer.name)
            events.emit(tick)
            if (tick.cancelled) {
                iterator.remove()
                events.emit(TimerStop(timer.name))
            }
        }
    }

    override fun stop(name: String) {
        if (queue.removeIf { it.name == name }) {
            events.emit(TimerStop(name))
        }
    }

    override fun clearAll() {
        for (timer in queue) {
            events.emit(TimerStop(timer.name))
        }
        queue.clear()
    }
}