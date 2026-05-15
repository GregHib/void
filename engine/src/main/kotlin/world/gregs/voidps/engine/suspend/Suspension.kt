package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

sealed class Suspension {

    /**
     * Wait for integer entry dialogue
     * p_countdialog
     */
    class IntEntry(private val continuation: CancellableContinuation<Int>) : Suspension() {
        fun resume(int: Int) = continuation.resume(int)
    }

    /**
     * Wait for string entry dialogue
     */
    class StringEntry(private val continuation: CancellableContinuation<String>) : Suspension() {
        fun resume(string: String) = continuation.resume(string)
    }

    /**
     * Wait for name entry dialogue
     */
    class NameEntry(private val continuation: CancellableContinuation<String>) : Suspension() {
        fun resume(string: String) = continuation.resume(string)
    }

    /**
     * Wait for "Click here to continue" dialogue
     * p_pausebutton
     */
    class Continue(private val continuation: CancellableContinuation<Unit>) : Suspension() {
        fun resume() = continuation.resume(Unit)
    }

    /**
     * Delay for [delay] ticks
     * p_delay
     */
    class Delay(private val continuation: CancellableContinuation<Unit>, delay: Int) : Suspension() {
        val tick = GameLoop.tick + delay

        fun ready(): Boolean = GameLoop.tick >= tick

        fun resume() = continuation.resume(Unit)
    }

    class Custom(private val continuation: CancellableContinuation<Unit>, val block: () -> Boolean) : Suspension() {

        fun ready(): Boolean = block.invoke()

        fun resume() = continuation.resume(Unit)
    }
}
