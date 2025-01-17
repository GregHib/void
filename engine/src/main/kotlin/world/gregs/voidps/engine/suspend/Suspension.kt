package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

abstract class Suspension {
    lateinit var continuation: CancellableContinuation<Unit>

    abstract fun ready(): Boolean

    open fun resume() {
        continuation.resume(Unit)
    }
}