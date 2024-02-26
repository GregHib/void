package world.gregs.voidps.engine.event

interface Event {
    open fun key(): String {
        return ""
    }
}