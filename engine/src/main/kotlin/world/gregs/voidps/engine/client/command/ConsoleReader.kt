package world.gregs.voidps.engine.client.command

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.Settings
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Reads operator commands from [terminal] and queues them for the game thread.
 *
 * Runs on a daemon thread so that a blocked read can never hold up the game loop, and never keeps
 * the jvm alive once Ctrl + C has triggered the shutdown hook.
 *
 * Without a terminal to take control of there's nowhere to hold the input line, so what's typed ends
 * up cut through by whatever the server logs and tab indents rather than completing. Gradle hands a
 * forked process pipes, and intellij's console isn't a terminal either, so the console stays out of
 * the way there unless [setting] asks for it.
 */
class ConsoleReader(
    private val terminal: ConsoleTerminal = SystemTerminal(),
    private val submit: (String) -> Unit = ConsoleCommands::submit,
    setting: String = Settings["console.enabled", AUTO],
) : Runnable {

    private val line = ConsoleLine(PROMPT, terminal::write) { terminal.width }
    private val disabled = setting.equals(NEVER, ignoreCase = true)
    private val piped = setting.equals(ALWAYS, ignoreCase = true)

    /**
     * Start reading commands, returning null when there's nothing to read them from.
     */
    fun start(): Thread? {
        if (disabled) {
            return null
        }
        if (terminal.start()) {
            // Logs are printed above the input line rather than on top of it
            ConsoleOutput.install(line::printAbove)
            terminal.onResize = line::show
        } else if (!piped) {
            logger.info { "No terminal to read commands from, console disabled. Start the server from a terminal for it, or set console.enabled=true to read commands piped in." }
            return null
        }
        Runtime.getRuntime().addShutdownHook(thread(start = false) { stop() })
        return thread(isDaemon = true, name = "console", block = ::run)
    }

    override fun run() {
        try {
            logger.info { "Console ready, type 'help' for a list of commands." }
            if (!terminal.interactive) {
                logger.info { "No terminal to draw into, commands are read a line at a time without a prompt." }
                plain()
                return
            }
            keys()
        } finally {
            stop()
        }
    }

    /**
     * Read keys as they're typed, drawing the input line back out as we go.
     */
    private fun keys() {
        line.show()
        while (true) {
            val code = try {
                terminal.read()
            } catch (e: IOException) {
                logger.debug(e) { "Console input closed." }
                return
            }
            // Reading returns -1 at end of stream, without which the loop would spin at 100% cpu
            if (code == -1) {
                logger.debug { "No console input available, stopping console reader." }
                return
            }
            when (val key = line.key(code)) {
                is ConsoleLine.Key.Submit -> entered(key.line)
                ConsoleLine.Key.EndOfFile -> return
                ConsoleLine.Key.Clear -> clearScreen()
                ConsoleLine.Key.None -> {}
            }
        }
    }

    /**
     * Read whole lines echoed by whatever is on the other end of the stream.
     */
    private fun plain() {
        val reader = terminal.reader()
        while (true) {
            val text = try {
                reader.readLine()
            } catch (e: IOException) {
                logger.debug(e) { "Console input closed." }
                return
            }
            // readLine returns null at end of stream; stdin is closed under `docker run` without -i,
            // under systemd and when output is piped. Without returning the loop spins at 100% cpu.
            if (text == null) {
                logger.debug { "No console input available, stopping console reader." }
                return
            }
            if (text.isBlank() || complete(text)) {
                continue
            }
            // Tabs typed in the middle of a line were only ever an indent
            val line = text.replace(TAB.toString(), "")
            if (clear(line)) {
                continue
            }
            submit(line)
        }
    }

    /**
     * Answer a tab in a line which has already been entered.
     *
     * Terminals which can't be drawn into hand over a whole line at a time, so the tab arrives as a
     * character in the middle of it rather than as a key press to complete on. Intellij's console on
     * windows is the common one; it indents rather than completing. The completion is printed for
     * the operator to type rather than run, since the line they meant is still ambiguous.
     *
     * Only a tab at the end of the line asks for that; carrying on typing after one means it was an
     * indent and the line is run with it taken out.
     */
    private fun complete(text: String): Boolean {
        val tab = text.indexOf(TAB)
        if (tab == -1 || text.substring(tab + 1).isNotBlank()) {
            return false
        }
        val typed = text.substring(0, tab)
        val completion = ConsoleCompleter.complete(typed, typed.length)
        val word = typed.substring(completion.start)
        if (completion.candidates.isEmpty()) {
            ConsoleCommands.output.invoke("No completions for '$word'.")
            return true
        }
        if (completion.candidates.size == 1) {
            ConsoleCommands.output.invoke(typed.substring(0, completion.start) + completion.candidates.first())
            return true
        }
        for (line in ConsoleCompleter.matches(completion.candidates)) {
            ConsoleCommands.output.invoke(line)
        }
        return true
    }

    private fun entered(text: String) {
        if (!terminal.watching) {
            // Nothing tells us about resizes, so catch up before the next line is drawn
            terminal.refreshWidth()
        }
        if (!text.isBlank() && !clear(text)) {
            submit(text)
        }
        line.show()
    }

    /**
     * Clear the screen, done here rather than on the game thread as it draws to the terminal.
     */
    private fun clear(text: String): Boolean {
        if (!text.trim().equals(ConsoleCommands.CLEAR, ignoreCase = true)) {
            return false
        }
        if (!terminal.interactive) {
            ConsoleCommands.output.invoke("Unable to clear without a terminal.")
            return true
        }
        clearScreen()
        return true
    }

    /**
     * Wipe the screen and the scrollback above it, then draw the input line back.
     */
    private fun clearScreen() {
        terminal.write(CLEAR_SCREEN)
        line.show()
    }

    private fun stop() {
        ConsoleOutput.uninstall()
        terminal.restore()
    }

    private companion object {
        private val logger = InlineLogger("Console")
        private const val PROMPT = "› "

        /**
         * Only take the console when there's a terminal for it, the default.
         */
        private const val AUTO = "auto"

        /**
         * Read lines piped in as well, for scripted input.
         */
        private const val ALWAYS = "true"

        private const val NEVER = "false"
        private const val TAB = '\t'


        /**
         * Cursor home, erase the screen, then erase the scrollback.
         */
        private const val CLEAR_SCREEN = "[H[2J[3J"
    }
}
