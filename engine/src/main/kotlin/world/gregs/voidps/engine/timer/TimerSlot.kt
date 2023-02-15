package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Events

class TimerSlot(
    private val events: Events
) : Timers {

    private var timer: Timer? = null

    override fun add(name: String, interval: Int, cancelExecution: Boolean, block: Timer.(Long) -> Unit) {
        val timer = Timer(name, interval, cancelExecution, block)
        set(timer)
        events.emit(TimerStart(timer.name))
    }

    private fun set(timer: Timer?) {
        val previous = this.timer
        this.timer = timer
        if (previous != null) {
            previous.cancel()
            events.emit(TimerStop(previous.name))
        }
    }

    override fun contains(name: String): Boolean {
        return timer?.name == name
    }

    override fun run() {
        val timer = timer ?: return
        if (!timer.ready()) {
            return
        }
        timer.resume()
        events.emit(TimerTick(timer.name, timer.count))
        if (timer.cancelled) {
            this.timer = null
            events.emit(TimerStop(timer.name))
        }
    }

    override fun clear(name: String) {
        if (contains(name)) {
            clearAll()
        }
    }

    override fun clearAll() {
        set(null)
    }
}