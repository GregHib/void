package world.gregs.voidps.engine.event.suspend

object InfiniteSuspension : EventSuspension {
    override fun ready(): Boolean {
        return false
    }

    override fun finished(): Boolean {
        return false
    }

    override fun resume() {
    }
}