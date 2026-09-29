package world.gregs.voidps.engine.client.command

import java.io.BufferedReader
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A [ConsoleTerminal] driven with `stty`, the way jline drives terminals it has no native access to.
 *
 * Reading a key at a time means turning off the terminal's own line editing; signals are left
 * enabled so that Ctrl + C still interrupts the jvm and runs its shutdown hooks as the readme
 * documents.
 *
 * Everything here fails softly: when there's no terminal to control - windows, `docker run` without
 * -i, systemd, gradle's pipes, or output redirected to a file - [start] returns false and the
 * console falls back to reading plain lines.
 */
class SystemTerminal(
    private val input: InputStream = System.`in`,
    private val out: PrintStream = PrintStream(FileOutputStream(FileDescriptor.out), false, Charsets.UTF_8),
) : ConsoleTerminal {

    private val closed = AtomicBoolean(false)
    private var settings: String? = null

    override var interactive: Boolean = false
        private set

    override var watching: Boolean = false
        private set

    override var width: Int = DEFAULT_WIDTH
        private set

    override var onResize: () -> Unit = {}

    override val attached: Boolean
        get() = attachedTerminal()

    override fun start(): Boolean {
        if (!attached) {
            return false
        }
        val saved = stty("-g") ?: return false
        // Line editing and echo off so keys arrive one at a time, flow control off so Ctrl + S
        // can't freeze the console; interrupts are left alone so Ctrl + C still stops the server
        if (stty("-icanon", "-echo", "-ixon", "min", "1", "time", "0") == null) {
            return false
        }
        settings = saved
        interactive = true
        refreshWidth()
        watchResize()
        return true
    }

    override fun read(): Int = input.read()

    override fun reader(): BufferedReader = input.bufferedReader()

    override fun write(text: String) {
        out.print(text)
        out.flush()
    }

    /**
     * Without this the shell is left without echo once the server exits. Safe to call from both the
     * reader and a shutdown hook.
     */
    override fun restore() {
        val saved = settings ?: return
        if (!closed.compareAndSet(false, true)) {
            return
        }
        interactive = false
        write("$SHOW_CURSOR\r\n")
        stty(saved)
    }

    override fun refreshWidth() {
        width = size(stty("size")) ?: return
    }

    /**
     * Resizes arrive as a signal which isn't available on every runtime; a jlink'd one without
     * jdk.unsupported fails to even load the class.
     */
    private fun watchResize() {
        try {
            sun.misc.Signal.handle(sun.misc.Signal("WINCH")) {
                refreshWidth()
                onResize.invoke()
            }
            watching = true
        } catch (e: Throwable) {
            watching = false
        }
    }

    /**
     * Run stty against this process' terminal, returning its output or null if it couldn't be run.
     *
     * Inheriting stdin is what points stty at the terminal the server is being typed into; given a
     * pipe instead it fails, which is exactly the fallback condition. Opening `/dev/tty` would work
     * even when input is piped, putting a terminal nothing is reading from into raw mode.
     */
    private fun stty(vararg args: String): String? {
        try {
            val process = ProcessBuilder(listOf(COMMAND) + args)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroy()
                return null
            }
            if (process.exitValue() != 0) {
                return null
            }
            return output
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        private const val COMMAND = "stty"
        private const val DEFAULT_WIDTH = 80
        private const val TIMEOUT_SECONDS = 1L
        private const val SHOW_CURSOR = "[?25h"

        /**
         * Columns from the "rows columns" stty prints.
         */
        internal fun size(output: String?): Int? {
            val columns = output?.split(" ")?.getOrNull(1)?.toIntOrNull() ?: return null
            return if (columns > 0) columns else null
        }
    }
}
