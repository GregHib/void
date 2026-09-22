package world.gregs.voidps.engine.client.command

import java.io.BufferedReader
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [ConsoleTerminal] driven through the windows console api, which has no `stty` to ask.
 *
 * Interrupts are left to the console as they are on other platforms, so Ctrl + C still stops the
 * server and runs its shutdown hooks. Reading keys as they're typed needs windows 10 1703 or newer;
 * an older console refuses it and is handed back to be read a line at a time instead.
 *
 * A console's mode belongs to the console rather than to the process, so one left in character mode
 * by a server which was killed outright stays that way until the window is closed. There's no
 * `stty sane` to put it right with.
 */
class WindowsTerminal internal constructor(
    private val input: InputStream = System.`in`,
    private val out: PrintStream = PrintStream(FileOutputStream(FileDescriptor.out), false, Charsets.UTF_8),
    private val console: ConsoleApi = Kernel32Api(),
) : ConsoleTerminal {

    private val closed = AtomicBoolean(false)
    private var keys: Int? = null
    private var drawing: Int? = null
    private var page: Int? = null

    override var interactive: Boolean = false
        private set

    /**
     * Windows has no resize signal, and the events which report one can't be read alongside keys,
     * so the width is asked for again as commands are entered.
     */
    override val watching: Boolean = false

    override var width: Int = DEFAULT_WIDTH
        private set

    override var onResize: () -> Unit = {}

    override val attached: Boolean
        get() = attached()

    override fun start(): Boolean {
        val input = console.handle(INPUT) ?: return false
        val output = console.handle(OUTPUT) ?: return false
        val keys = console.mode(input) ?: return false
        val drawing = console.mode(output) ?: return false
        if (!console.mode(input, keyMode(keys))) {
            return false
        }
        if (!console.mode(output, drawMode(drawing))) {
            console.mode(input, keys)
            return false
        }
        this.keys = keys
        this.drawing = drawing
        // The prompt and the gutter a reply is written with are utf-8, which a console left on its
        // own code page draws as something else entirely
        page = console.codePage()?.takeIf { page -> page != UTF_8 && console.codePage(UTF_8) }
        interactive = true
        refreshWidth()
        return true
    }

    override fun read(): Int = input.read()

    override fun reader(): BufferedReader = input.bufferedReader()

    override fun write(text: String) {
        out.print(text)
        out.flush()
    }

    /**
     * Without this the console is left with nothing echoed back into it once the server exits. Safe
     * to call from both the reader and a shutdown hook.
     */
    override fun restore() {
        val keys = this.keys ?: return
        if (!closed.compareAndSet(false, true)) {
            return
        }
        interactive = false
        // While the console still understands it
        write("$SHOW_CURSOR\r\n")
        val output = console.handle(OUTPUT)
        if (output != null) {
            drawing?.let { drawing -> console.mode(output, drawing) }
        }
        console.handle(INPUT)?.let { input -> console.mode(input, keys) }
        page?.let { page -> console.codePage(page) }
    }

    override fun refreshWidth() {
        val output = console.handle(OUTPUT) ?: return
        width = console.columns(output)?.takeIf { columns -> columns > 0 } ?: return
    }

    /**
     * Whether a console is attached to both ends, so output redirected to a file isn't drawn into
     * even though the input side is a console.
     */
    private fun attached(): Boolean {
        if (!console.available) {
            // Nothing to ask, so fall back to what the other platforms use rather than report no
            // terminal at all; the console is read a line at a time instead of not at all
            return attachedTerminal()
        }
        val input = console.handle(INPUT) ?: return false
        val output = console.handle(OUTPUT) ?: return false
        return console.mode(input) != null && console.mode(output) != null
    }

    internal companion object {
        private const val DEFAULT_WIDTH = 80
        private const val SHOW_CURSOR = "\u001b[?25h"
        private const val UTF_8 = 65001
        private const val INPUT = -10
        private const val OUTPUT = -11

        private const val PROCESSED_INPUT = 0x0001
        private const val LINE_INPUT = 0x0002
        private const val ECHO_INPUT = 0x0004
        private const val WINDOW_INPUT = 0x0008
        private const val MOUSE_INPUT = 0x0010
        private const val QUICK_EDIT = 0x0040
        private const val EXTENDED_FLAGS = 0x0080
        private const val VIRTUAL_TERMINAL_INPUT = 0x0200

        private const val PROCESSED_OUTPUT = 0x0001
        private const val WRAP_AT_END = 0x0002
        private const val VIRTUAL_TERMINAL_PROCESSING = 0x0004
        private const val NEWLINE_AUTO_RETURN = 0x0008

        /**
         * Keys a at a time, drawn by us rather than by the console, and arriving as the escape
         * sequences the input line already reads.
         *
         * Interrupts are deliberately left processed, which is what keeps Ctrl + C stopping the
         * server. Quick edit goes because a stray click in the window otherwise selects text and
         * blocks everything the server writes until it's dismissed; the extended flag has to be set
         * for that to be taken any notice of.
         */
        internal fun keyMode(saved: Int): Int {
            val wanted = saved or PROCESSED_INPUT or EXTENDED_FLAGS or VIRTUAL_TERMINAL_INPUT
            return wanted and (LINE_INPUT or ECHO_INPUT or WINDOW_INPUT or MOUSE_INPUT or QUICK_EDIT).inv()
        }

        /**
         * Escape sequences understood rather than printed, and lines still wrapping at the end of
         * the window: without that the cursor waits at the last column instead of moving on, which
         * is the case the input line's own wrapping already works around.
         */
        internal fun drawMode(saved: Int): Int {
            val wanted = saved or PROCESSED_OUTPUT or WRAP_AT_END or VIRTUAL_TERMINAL_PROCESSING
            return wanted and NEWLINE_AUTO_RETURN.inv()
        }
    }
}
