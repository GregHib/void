package world.gregs.voidps.engine.suspend

abstract class Suspension {
    open var dialogue: Boolean = false
    var finished: Boolean = false
        private set

    abstract fun ready(): Boolean

    open fun resume() {
        finished = true
    }
}