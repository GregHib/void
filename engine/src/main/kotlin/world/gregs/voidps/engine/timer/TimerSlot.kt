package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.event.Events

class TimerSlot(
    private val events: Events
) : Timers {

    private var timer: Timer? = null

    override fun start(name: String, restart: Boolean) {
        val start = TimerStart(name, restart)
        events.emit(start)
        set(Timer(name, start.interval))
    }

    private fun set(timer: Timer?) {
        val previous = this.timer
        this.timer = timer
        if (previous != null) {
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
        timer.reset()
        val tick = TimerTick(timer.name)
        events.emit(tick)
        if (tick.cancelled) {
            set(null)
        }
    }

    override fun stop(name: String) {
        if (contains(name)) {
            clearAll()
        }
    }

    override fun clearAll() {
        set(null)
    }
}