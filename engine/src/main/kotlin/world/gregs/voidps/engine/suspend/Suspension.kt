package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import world.gregs.voidps.engine.GameLoop
import kotlin.coroutines.resume

sealed class Suspension {

    /**
     * Abandon the suspended action without resuming it, for suspensions which need input that can't be supplied
     */
    abstract fun cancel()

    /**
     * Wait for integer entry dialogue
     * p_countdialog
     */
    class IntEntry(private val continuation: CancellableContinuation<Int>) : Suspension() {
        fun resume(int: Int) {
            if (continuation.isCancelled) {
                return
            }
            continuation.resume(int)
        }

        override fun cancel() {
            continuation.cancel()
        }
    }

    /**
     * Wait for string entry dialogue
     */
    class StringEntry(private val continuation: CancellableContinuation<String>) : Suspension() {
        fun resume(string: String) {
            if (continuation.isCancelled) {
                return
            }
            continuation.resume(string)
        }

        override fun cancel() {
            continuation.cancel()
        }
    }

    /**
     * Wait for name entry dialogue
     */
    class NameEntry(private val continuation: CancellableContinuation<String>) : Suspension() {
        fun resume(string: String) {
            if (continuation.isCancelled) {
                return
            }
            continuation.resume(string)
        }
        override fun cancel() {
            continuation.cancel()
        }
    }

    /**
     * Wait for "Click here to continue" dialogue
     * p_pausebutton
     */
    class Continue(private val continuation: CancellableContinuation<Unit>) : Suspension() {
        fun resume() {
            if (continuation.isCancelled) {
                return
            }
            continuation.resume(Unit)
        }
        override fun cancel() {
            continuation.cancel()
        }
    }

    /**
     * Delay for [delay] ticks
     * p_delay
     */
    class Delay(private val continuation: CancellableContinuation<Unit>, delay: Int) : Suspension() {
        val tick = GameLoop.tick + delay

        fun ready(): Boolean = GameLoop.tick >= tick

        fun resume() {
            if (continuation.isCancelled) {
                return
            }
            continuation.resume(Unit)
        }
        override fun cancel() {
            continuation.cancel()
        }
    }

    class Custom(private val continuation: CancellableContinuation<Unit>, val block: () -> Boolean) : Suspension() {

        fun ready(): Boolean = block.invoke()

        fun resume() {
            if (continuation.isCancelled) {
                return
            }
            continuation.resume(Unit)
        }

        override fun cancel() {
            continuation.cancel()
        }
    }
}
