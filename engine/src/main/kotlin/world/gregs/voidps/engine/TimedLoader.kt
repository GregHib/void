package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.plural

private val logger = InlineLogger("TimedLoader")

fun <R> timed(name: String, block: () -> R): R {
    val start = System.currentTimeMillis()
    val result = block.invoke()
    val duration = System.currentTimeMillis() - start
    logger.info { "Loaded $name in ${duration}ms" }
    return result
}

fun timedLoad(name: String, block: () -> Int) {
    val start = System.currentTimeMillis()
    val result = block.invoke()
    timedLoad(name, result, start)
}

fun timedLoad(name: String, result: Int, start: Long) {
    val duration = System.currentTimeMillis() - start
    logger.info { "Loaded $result ${name.plural(result)} in ${duration}ms" }
}
