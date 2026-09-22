package world.gregs.voidps.engine.client.command

import java.io.BufferedReader
import java.io.ByteArrayInputStream

/**
 * A terminal fed from a string, so the console can be driven without one attached.
 */
class FakeTerminal(
    input: String,
    override val interactive: Boolean = false,
    override val width: Int = 80,
    override val attached: Boolean = interactive,
) : ConsoleTerminal {

    private val stream = ByteArrayInputStream(input.toByteArray())
    val written = StringBuilder()
    var restored = false
        private set
    var refreshes = 0
        private set

    override val watching: Boolean = false

    override var onResize: () -> Unit = {}

    override fun start(): Boolean = interactive

    override fun read(): Int = stream.read()

    override fun reader(): BufferedReader = stream.bufferedReader()

    override fun write(text: String) {
        written.append(text)
    }

    override fun refreshWidth() {
        refreshes++
    }

    override fun restore() {
        restored = true
    }
}
