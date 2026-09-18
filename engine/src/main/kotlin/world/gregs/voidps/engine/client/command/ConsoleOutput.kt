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
        private var original: PrintStream? = null

        /**
         * Redirect everything written to `System.out` through [print], above the input line.
         */
        fun install(print: (String) -> Unit) {
            if (original != null) {
                return
            }
            original = System.out
            System.setOut(PrintStream(ConsoleOutput(print), true))
        }

        fun uninstall() {
            val out = original ?: return
            System.setOut(out)
            original = null
        }
    }
}
