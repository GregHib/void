package world.gregs.voidps.engine.client.command

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

/**
 * Wraps `System.out` so each line logged is printed above the console's input line instead of on top
 * of it, keeping the prompt at the bottom of the terminal.
 *
 * Lines are buffered because [print] takes a whole line at a time.
 */
class ConsoleOutput(
    private val print: (String) -> Unit,
) : OutputStream() {

    private val line = ByteArrayOutputStream()

    @Synchronized
    override fun write(byte: Int) {
        if (byte == NEW_LINE) {
            printLine()
            return
        }
        line.write(byte)
    }

    @Synchronized
    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        for (index in offset until offset + length) {
            write(bytes[index].toInt())
        }
    }

    private fun printLine() {
        val text = line.toString(Charsets.UTF_8).trimEnd('\r')
        line.reset()
        print(text)
    }

    companion object {
        private const val NEW_LINE = '\n'.code
        private var out: PrintStream? = null
        private var error: PrintStream? = null

        /**
         * Redirect everything written to `System.out` and `System.err` through [print], above the
         * input line.
         *
         * Logging goes to stdout, but stack traces printed straight to stderr - `printStackTrace`
         * and anything uncaught off the game thread - would otherwise land on top of the prompt.
         */
        fun install(print: (String) -> Unit) {
            if (out != null) {
                return
            }
            out = System.out
            error = System.err
            System.setOut(PrintStream(ConsoleOutput(print), true))
            System.setErr(PrintStream(ConsoleOutput(print), true))
        }

        fun uninstall() {
            System.setOut(out ?: return)
            System.setErr(error ?: return)
            out = null
            error = null
        }
    }
}
