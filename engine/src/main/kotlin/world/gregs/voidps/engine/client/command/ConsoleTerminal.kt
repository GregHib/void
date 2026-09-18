package world.gregs.voidps.engine.client.command

import java.io.BufferedReader

/**
 * The terminal the server is being operated from, see [SystemTerminal].
 */
interface ConsoleTerminal {

    /**
     * Whether keys can be read one at a time and drawn back to the terminal.
     */
    val interactive: Boolean

    /**
     * Whether resizes are watched, otherwise [refreshWidth] has to be called by hand.
     */
    val watching: Boolean

    val width: Int

    /**
     * Take control of the terminal, returning false when there isn't one to take.
     */
    fun start(): Boolean

    /**
     * Read a single byte, -1 at the end of the stream.
     */
    fun read(): Int

    /**
     * Read whole lines, for when there's no terminal to draw into.
     */
    fun reader(): BufferedReader

    fun write(text: String)

    fun refreshWidth()

    /**
     * Called when the terminal is resized, so the input line can be drawn again at the new width.
     */
    var onResize: () -> Unit

    /**
     * Hand the terminal back in the state it was found in.
     */
    fun restore()
}
