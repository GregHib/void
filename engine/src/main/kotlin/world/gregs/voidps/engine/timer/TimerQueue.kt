package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Events
import java.util.*

class TimerQueue(
    private val events: Events
) : Timers {

    private val queue = PriorityQueue<Timer>()

    override fun start(name: String, interval: Int, cancelExecution: Boolean, persist: Boolean, block: Timer.(Long) -> Unit) {
        val timer = Timer(name, interval, cancelExecution, block)
        queue.add(timer)
        events.emit(TimerStart(timer.name))
    }

    override fun contains(name: String): Boolean {
        return queue.any { it.name == name }
    }

    override fun run() {
        val it = queue.iterator()
        var timer: Timer
        while (it.hasNext()) {
            timer = it.next()
            if (!timer.ready()) {
                break
            }
            timer.resume()
            events.emit(TimerTick(timer.name, timer.count))
            if (timer.cancelled) {
                it.remove()
                events.emit(TimerStop(timer.name))
            }
        }
    }

    override fun stop(name: String) {
        queue.removeIf { timer ->
            if (timer.name == name) {
                timer.cancel()
                events.emit(TimerStop(timer.name))
                true
            } else {
                false
            }
        }
    }

    override fun clearAll() {
        for (timer in queue) {
            timer.cancel()
            events.emit(TimerStop(timer.name))
        }
        queue.clear()
    }
}