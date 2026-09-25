package world.gregs.voidps.engine.client.command

import org.fusesource.jansi.internal.Kernel32
import org.fusesource.jansi.internal.Kernel32.CONSOLE_SCREEN_BUFFER_INFO

/**
 * The windows console api, as much of it as [WindowsTerminal] needs.
 *
 * Kept behind an interface so the terminal's own decisions can be tested away from windows, and so
 * that the one file naming a native class is the one below.
 */
internal interface ConsoleApi {

    /**
     * Whether the calls can be made at all; the library behind them isn't on every platform.
     */
    val available: Boolean

    /**
     * A standard handle, or null when nothing is attached to it.
     */
    fun handle(id: Int): Long?

    /**
     * A handle's console mode, or null when the handle isn't a console.
     */
    fun mode(handle: Long): Int?

    fun mode(handle: Long, mode: Int): Boolean

    /**
     * How many columns wide the console's buffer is, which is where a line being typed wraps.
     */
    fun columns(handle: Long): Int?

    fun codePage(): Int?

    fun codePage(page: Int): Boolean
}

/**
 * Every call is guarded: without the library a call raises an error rather than an exception, and
 * jansi loads gracefully by default, so nothing fails until the first one is made.
 */
internal class Kernel32Api : ConsoleApi {

    override val available: Boolean = try {
        Kernel32.GetStdHandle(0)
        true
    } catch (e: Throwable) {
        false
    }

    override fun handle(id: Int): Long? {
        try {
            val handle = Kernel32.GetStdHandle(id)
            return if (handle == NONE || handle == INVALID) null else handle
        } catch (e: Throwable) {
            return null
        }
    }

    override fun mode(handle: Long): Int? {
        try {
            val mode = IntArray(1)
            return if (Kernel32.GetConsoleMode(handle, mode) == 0) null else mode[0]
        } catch (e: Throwable) {
            return null
        }
    }

    override fun mode(handle: Long, mode: Int): Boolean {
        try {
            return Kernel32.SetConsoleMode(handle, mode) != 0
        } catch (e: Throwable) {
            return false
        }
    }

    override fun columns(handle: Long): Int? {
        try {
            val info = CONSOLE_SCREEN_BUFFER_INFO()
            if (Kernel32.GetConsoleScreenBufferInfo(handle, info) == 0) {
                return null
            }
            return info.size.x.toInt()
        } catch (e: Throwable) {
            return null
        }
    }

    override fun codePage(): Int? {
        try {
            return Kernel32.GetConsoleOutputCP().takeIf { page -> page > 0 }
        } catch (e: Throwable) {
            return null
        }
    }

    override fun codePage(page: Int): Boolean {
        try {
            return Kernel32.SetConsoleOutputCP(page) != 0
        } catch (e: Throwable) {
            return false
        }
    }

    private companion object {
        private const val NONE = 0L
        private const val INVALID = -1L
    }
}
