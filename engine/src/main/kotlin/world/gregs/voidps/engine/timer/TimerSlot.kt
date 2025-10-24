package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.npc.NPC

class TimerSlot(
    private val npc: NPC,
) : Timers {

    private var timer: Timer? = null

    override fun start(name: String, restart: Boolean): Boolean {
        val interval = TimerApi.start(npc, name, restart)
        if (interval == TimerApi.CANCEL || interval == TimerApi.REPEAT) {
            return false
        }
        if (timer != null) {
            TimerApi.stop(npc, timer!!.name, death = false)
        }
        this.timer = Timer(name, interval)
        return true
    }

    override fun contains(name: String): Boolean = timer?.name == name

    override fun run() {
        val timer = timer ?: return
        if (!timer.ready()) {
            return
        }
        timer.reset()
        val interval = TimerApi.tick(npc, timer.name)
        if (interval == TimerApi.CANCEL) {
            TimerApi.stop(npc, timer.name, death = false)
            this.timer = null
        } else if (interval != TimerApi.REPEAT) {
            timer.next(interval)
        }
    }

    override fun stop(name: String) {
        if (contains(name)) {
            TimerApi.stop(npc, timer!!.name, death = false)
            timer = null
        }
    }

    override fun clear(name: String): Boolean {
        if (contains(name)) {
            timer = null
            return true
        }
        return false
    }

    override fun clearAll() {
        timer = null
    }

    override fun stopAll() {
        if (timer != null) {
            TimerApi.stop(npc, timer!!.name, death = true)
        }
        timer = null
    }
}
