package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.Entity
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A logger for storing a record of game information
 * Note: not thread safe use only within game thread.
 */
object Log {
    private const val LOG_BUFFER_SIZE = 8192
    private val logs = ArrayList<String>(LOG_BUFFER_SIZE)

    fun event(source: Entity, action: String, vararg context: Any) {
        add {
            append(source).append("\t")
            append(action)
            for (info in context) {
                append("\t").append(info)
            }
        }
    }

    fun info(message: String) {
        add {
            append("System").append("\t")
            append(message)
        }
    }

    private fun add(block: StringBuilder.() -> Unit) {
        logs.add(buildString {
            append(System.nanoTime()).append("\t")
            append(GameLoop.tick).append("\t")
            block()
        })
    }

    private val ISO_LOCAL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")

    fun save(directory: File, now: LocalDateTime = LocalDateTime.now()) {
        if (logs.isEmpty()) {
            return
        }
        val hourTime = now.withMinute(0).withSecond(0).withNano(0)
        val file = directory.resolve("${ISO_LOCAL_FORMAT.format(hourTime)}.txt")
        file.appendText(buildString {
            for (log in logs) {
                appendLine()
            }
        })
        logs.clear()
    }

}
