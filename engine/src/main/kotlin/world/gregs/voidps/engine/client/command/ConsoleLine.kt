package world.gregs.voidps.engine.client.command

/**
 * The console's input line; what's been typed, where the cursor is and what's been typed before.
 *
 * The terminal echoes nothing while [ConsoleTerminal] has control of it, so every keystroke is drawn
 * back out through [write]. Keeping the line's state here is also what lets [printAbove] move a log
 * line in above whatever is half typed.
 */
class ConsoleLine(
    private val prompt: String,
    private val write: (String) -> Unit,
    private val width: () -> Int,
) {

    private val buffer = StringBuilder()
    private val history = mutableListOf<String>()
    private val pending = ByteArray(4)
    private var cursor = 0
    private var historyIndex = 0
    private var pendingSize = 0
    private var escape = Escape.None
    private var drawnRow = 0
    private var drawnWidth = 0

    /**
     * What a key press means to the console, anything else is handled by the line itself.
     */
    sealed interface Key {
        data object None : Key

        data class Submit(val line: String) : Key

        data object EndOfFile : Key

        data object Clear : Key
    }

    private enum class Escape { None, Started, Control }

    /**
     * Draw the prompt and anything typed into it.
     */
    @Synchronized
    fun show() {
        redraw()
    }

    /**
     * Handle a byte read from the terminal.
     */
    @Synchronized
    fun key(code: Int): Key {
        if (escape != Escape.None) {
            control(code)
            return Key.None
        }
        return when (code) {
            ESCAPE -> {
                escape = Escape.Started
                Key.None
            }
            CARRIAGE_RETURN, NEW_LINE -> submit()
            BACKSPACE, DELETE -> {
                backspace()
                Key.None
            }
            END_OF_FILE -> if (buffer.isEmpty()) Key.EndOfFile else Key.None
            FORM_FEED -> Key.Clear
            LINE_START -> {
                jump(0)
                Key.None
            }
            LINE_END -> {
                jump(buffer.length)
                Key.None
            }
            KILL_TO_START -> {
                kill(0, cursor)
                Key.None
            }
            KILL_TO_END -> {
                kill(cursor, buffer.length)
                Key.None
            }
            KILL_WORD -> {
                kill(word(), cursor)
                Key.None
            }
            TAB -> {
                complete()
                Key.None
            }
            in PRINTABLE -> {
                insert(code)
                Key.None
            }
            else -> Key.None
        }
    }

    /**
     * Print [text] above the input line, leaving what's been typed in place below it.
     */
    @Synchronized
    fun printAbove(text: String) {
        write("${erase()}$text\r\n${render()}")
    }

    private fun submit(): Key {
        val text = buffer.toString()
        cursor = buffer.length
        write("${erase()}$prompt$text\r\n")
        drawnRow = 0
        buffer.setLength(0)
        cursor = 0
        if (text.isNotBlank()) {
            history.remove(text)
            history.add(text)
            if (history.size > MAX_HISTORY) {
                history.removeAt(0)
            }
        }
        historyIndex = history.size
        return Key.Submit(text)
    }

    /**
     * Multi-byte characters arrive a byte at a time, so they're held until complete.
     */
    private fun insert(code: Int) {
        val byte = code.toByte()
        if (pendingSize > 0 || code >= CONTINUATION) {
            pending[pendingSize++] = byte
            val expected = length(pending[0])
            if (pendingSize < expected && pendingSize < pending.size) {
                return
            }
            val text = String(pending, 0, pendingSize, Charsets.UTF_8)
            pendingSize = 0
            buffer.insert(cursor, text)
            cursor += text.length
            redraw()
            return
        }
        buffer.insert(cursor, code.toChar())
        cursor++
        redraw()
    }

    private fun length(lead: Byte): Int {
        val value = lead.toInt() and 0xff
        return when {
            value >= 0xf0 -> 4
            value >= 0xe0 -> 3
            value >= 0xc0 -> 2
            else -> 1
        }
    }

    private fun backspace() {
        if (cursor == 0) {
            return
        }
        buffer.deleteCharAt(cursor - 1)
        cursor--
        redraw()
    }

    /**
     * Remove everything between [start] and [end], leaving the cursor where the text was.
     */
    private fun kill(start: Int, end: Int) {
        if (start >= end) {
            return
        }
        buffer.delete(start, end)
        cursor = start
        redraw()
    }

    /**
     * Where the word before the cursor starts, skipping any spaces it's sat behind.
     */
    private fun word(): Int {
        var index = cursor
        while (index > 0 && buffer[index - 1] == ' ') {
            index--
        }
        while (index > 0 && buffer[index - 1] != ' ') {
            index--
        }
        return index
    }

    private fun delete() {
        if (cursor >= buffer.length) {
            return
        }
        buffer.deleteCharAt(cursor)
        redraw()
    }

    /**
     * Handle the tail of an escape sequence e.g. the arrow keys.
     */
    private fun control(code: Int) {
        if (escape == Escape.Started) {
            // Cursor keys arrive as either form depending on the terminal's cursor key mode
            escape = if (code == CONTROL_SEQUENCE || code == CURSOR_SEQUENCE) Escape.Control else Escape.None
            return
        }
        if (code == DELETE_PARAMETER) {
            delete()
            escape = Escape.None
            return
        }
        // Parameters come before the character which ends the sequence
        if (code < FINAL_BYTE) {
            return
        }
        escape = Escape.None
        when (code.toChar()) {
            'A' -> previous()
            'B' -> next()
            'C' -> move(1)
            'D' -> move(-1)
            'H' -> jump(0)
            'F' -> jump(buffer.length)
        }
    }

    private fun move(amount: Int) {
        val position = cursor + amount
        if (position < 0 || position > buffer.length) {
            return
        }
        cursor = position
        redraw()
    }

    private fun jump(position: Int) {
        cursor = position
        redraw()
    }

    private fun previous() {
        if (historyIndex == 0) {
            return
        }
        historyIndex--
        replace(history[historyIndex])
    }

    private fun next() {
        if (historyIndex >= history.size) {
            return
        }
        historyIndex++
        replace(history.getOrElse(historyIndex) { "" })
    }

    private fun replace(text: String) {
        val erase = erase()
        buffer.setLength(0)
        buffer.append(text)
        cursor = buffer.length
        write("$erase${render()}")
    }

    private fun complete() {
        val completion = ConsoleCompleter.complete(buffer.toString(), cursor)
        if (completion.candidates.isEmpty()) {
            return
        }
        val match = if (completion.candidates.size == 1) {
            completion.candidates.first()
        } else {
            matches(completion.candidates)
            longestCommonPrefix(completion.candidates) ?: return
        }
        val quoted = if (match.contains(' ')) "\"$match\"" else match
        val suffix = if (completion.candidates.size == 1) " " else ""
        buffer.replace(completion.start, cursor, quoted + suffix)
        cursor = completion.start + quoted.length + suffix.length
        redraw()
    }

    /**
     * List what the word being typed could be, above the input line.
     */
    private fun matches(candidates: List<String>) {
        val shown = candidates.take(MAX_MATCHES)
        val header = if (candidates.size > shown.size) "Matches (showing ${shown.size} of ${candidates.size}):" else "Matches:"
        val lines = shown.joinToString("\r\n") { candidate -> "  $candidate" }
        write("${erase()}$header\r\n$lines\r\n${render()}")
    }

    private fun redraw() {
        write("${erase()}${render()}")
    }

    /**
     * Move back to where the prompt starts and wipe everything below it, which takes the input line
     * with it however many rows it wrapped over.
     *
     * Which row the cursor is on is remembered from the last draw rather than worked out again; the
     * terminal reflows what's already on screen when it's resized, so the rows a wider or narrower
     * window would have used say nothing about where the cursor actually is.
     */
    private fun erase(): String {
        val up = if (drawnWidth == width() && drawnRow > 0) "$ESCAPE_SEQUENCE${drawnRow}A" else ""
        return "$up$PROMPT_START$ERASE_BELOW"
    }

    /**
     * The prompt, what's been typed, then the cursor moved back to where it's editing.
     */
    private fun render(): String {
        val columns = width()
        val end = prompt.length + buffer.length
        val position = prompt.length + cursor
        // Filling a row exactly leaves the cursor pending on the end of it rather than wrapping, so
        // force the wrap to keep the terminal and the maths below agreeing on which row it's on
        val wrap = if (end > 0 && end % columns == 0) WRAP else ""
        val up = (end / columns) - (position / columns)
        val rows = if (up > 0) "$ESCAPE_SEQUENCE${up}A" else ""
        val column = position % columns
        val forward = if (column > 0) "$ESCAPE_SEQUENCE${column}C" else ""
        drawnRow = position / columns
        drawnWidth = columns
        return "$prompt$buffer$wrap$rows$PROMPT_START$forward"
    }

    private companion object {
        private const val MAX_MATCHES = 10
        private const val MAX_HISTORY = 100
        private const val TAB = 9
        private const val NEW_LINE = 10
        private const val FORM_FEED = 12
        private const val CARRIAGE_RETURN = 13
        private const val BACKSPACE = 8
        private const val DELETE = 127
        private const val END_OF_FILE = 4
        private const val LINE_START = 1
        private const val LINE_END = 5
        private const val KILL_TO_END = 11
        private const val KILL_TO_START = 21
        private const val KILL_WORD = 23
        private const val ESCAPE = 27
        private const val CONTROL_SEQUENCE = '['.code
        private const val CURSOR_SEQUENCE = 'O'.code
        private const val DELETE_PARAMETER = '~'.code
        private const val FINAL_BYTE = 0x40
        private const val CONTINUATION = 0x80
        private val PRINTABLE = 32..255
        private const val ESCAPE_SEQUENCE = "["
        private const val PROMPT_START = "\r"
        private const val ERASE_BELOW = "${ESCAPE_SEQUENCE}J"
        private const val WRAP = " \b"
    }
}
