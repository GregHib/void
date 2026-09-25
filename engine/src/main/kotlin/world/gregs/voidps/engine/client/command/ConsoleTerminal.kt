package world.gregs.voidps.engine.client.command

import java.io.BufferedReader

/**
 * The terminal the server is being operated from, see [SystemTerminal] and [WindowsTerminal].
 */
interface ConsoleTerminal {

    /**
     * Whether the server was started from a terminal at all, whether or not [start] can take it
     * over. Windows has one but no `stty` to control it with.
     */
    val attached: Boolean

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

/**
 * The terminal for whichever platform the server is running on; windows consoles are driven through
 * the console api rather than `stty`.
 */
fun consoleTerminal(os: String = System.getProperty("os.name", "")): ConsoleTerminal = if (os.startsWith("Windows", ignoreCase = true)) WindowsTerminal() else SystemTerminal()

/**
 * Whether input and output are both a terminal, so output piped to a file or through tee isn't
 * drawn into even though the input side would happily be taken.
 */
internal fun attachedTerminal(): Boolean {
    val console = System.console() ?: return false
    // Java 22 hands back a console whether or not it's a terminal, and tells you which
    try {
        return console.javaClass.getMethod("isTerminal").invoke(console) as Boolean
    } catch (e: ReflectiveOperationException) {
        return true
    }
}
