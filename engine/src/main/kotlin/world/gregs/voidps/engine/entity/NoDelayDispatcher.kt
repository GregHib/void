package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.dispatch.MapDispatcher

class NoDelayDispatcher<T : Any>(private val noDelays: MutableSet<T>) : MapDispatcher<T>("@Approach") {
    override fun process(instance: T, annotation: String, arguments: String) {
        if (annotation == "@NoDelay") {
            noDelays.add(instance)
            return
        }
        super.process(instance, annotation, arguments)
    }
}