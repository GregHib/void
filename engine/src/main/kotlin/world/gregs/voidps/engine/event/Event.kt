package world.gregs.voidps.engine.event

interface Event {

    open fun size(): Int {
        return 0
    }

    open fun parameter(dispatcher: EventDispatcher, index: Int): Any? {
        return null
    }

    fun debug(dispatcher: EventDispatcher): String = Array(size()) { parameter(dispatcher, it) }.contentToString()
}