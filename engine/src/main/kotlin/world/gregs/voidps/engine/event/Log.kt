package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.GameLoop
import java.io.File

object Log {
    private const val LOG_BUFFER_SIZE = 4_096
    private val logs = arrayOfNulls<String>(LOG_BUFFER_SIZE)
    private var index = 0
    var level = LogLevel.Info

    inline fun info(msg: String) {
        if (level == LogLevel.Info) {

        }
    }

    fun log(message: String) {
        logs[index++.mod(LOG_BUFFER_SIZE)] = "nanoTime\ttick\tlevel\tsourceType\tcategory\tmessage"
        logs[index++.mod(LOG_BUFFER_SIZE)] = "${System.nanoTime()}\t${GameLoop.tick}\t$message"
    }

    fun save(file: File) {
        file.appendText(buildString {
            for (line in index.rem(LOG_BUFFER_SIZE) until index) {
                appendLine(logs[line])
            }
        })
    }

    fun clear() {
        index = 0
    }
}

enum class LogLevel {
    Info,
    Debug,
    Verbose,
}

enum class SourceType {
    Player,
    NPC,
    World,
    Inventory,
    Item,
    GameObject,
    FloorItem,
    Event
}

enum class LogCategory {
    Player,
    NPC,
    World,
    Inventory,
    Item,
    GameObject,
    FloorItem,
    Event
}
