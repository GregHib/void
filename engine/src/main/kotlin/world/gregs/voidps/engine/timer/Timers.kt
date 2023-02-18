package world.gregs.voidps.engine.timer

interface Timers : Runnable {
    fun restart(name: String) = start(name, restart = true)
    fun start(name: String, restart: Boolean = false): Boolean
    fun contains(name: String): Boolean
    fun stop(name: String)
    fun clearAll()

    fun toggle(name: String) {
        if (contains(name)) {
            stop(name)
            return
        }
        start(name)
    }

    fun startIfAbsent(name: String) {
        if (contains(name)) {
            return
        }
        start(name)
    }
}