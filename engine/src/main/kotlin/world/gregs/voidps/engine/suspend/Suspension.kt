package world.gregs.voidps.engine.suspend

abstract class Suspension {
    protected abstract val onCancel: (() -> Unit)?
    open var dialogue: Boolean = false

    abstract fun ready(): Boolean

    open fun resume() {
    }

    open fun cancel() {
        onCancel?.invoke()
    }
}