package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.utility.plural

private val logger = InlineLogger("TimedLoader")

fun timedLoad(name: String, block: () -> Int) {
    val start = System.currentTimeMillis()
    val result = block.invoke()
    val duration = System.currentTimeMillis() - start
    logger.info { "Loaded $result ${name.plural(result)} in ${duration}ms" }
}