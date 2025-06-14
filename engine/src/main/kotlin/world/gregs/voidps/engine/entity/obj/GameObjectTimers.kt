package world.gregs.voidps.engine.entity.obj

class GameObjectTimers : Runnable {
    internal data class Timer(
        val objs: Set<GameObject>,
        var ticks: Int,
        val block: () -> Unit,
    )

    private val timers: MutableList<Timer> = mutableListOf()

    override fun run() {
        timers.removeIf { timer ->
            if (--timer.ticks == 0) {
                timer.block.invoke()
            }
            timer.ticks <= 0
        }
    }

    fun add(gameObject: GameObject, ticks: Int, block: () -> Unit) {
        if (gameObject.intId == -1 || ticks <= 0) {
            return
        }
        cancel(gameObject)
        timers.add(Timer(setOf(gameObject), ticks, block))
    }

    fun add(gameObjects: Set<GameObject>, ticks: Int, block: () -> Unit) {
        if (gameObjects.any { it.intId == -1 } || ticks <= 0) {
            return
        }
        for (obj in gameObjects) {
            cancel(obj)
        }
        timers.add(Timer(gameObjects, ticks, block))
    }

    fun cancel(gameObject: GameObject): Boolean = timers.removeIf { it.objs.contains(gameObject) }

    fun execute(gameObject: GameObject): Boolean = timers.removeIf {
        if (it.objs.contains(gameObject)) {
            it.block.invoke()
            true
        } else {
            false
        }
    }

    /**
     * Fires all timers to reset all temporary objects to their original states
     */
    fun reset() {
        timers.forEach {
            it.block.invoke()
        }
        timers.clear()
    }
}
