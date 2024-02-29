package world.gregs.voidps.engine.event

interface Event {
    open fun parameters(dispatcher: EventDispatcher): Array<String> {
        return emptyArray()
    }
}