package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import org.koin.core.time.measureDurationForResult
import world.gregs.voidps.engine.utility.plural

private val logger = InlineLogger("TimedLoader")

fun timedLoad(name: String, block: () -> Int) {
    val (result, time) = measureDurationForResult {
        block.invoke()
    }
    logger.info { "Loaded $result ${name.plural(result)} in ${time.toInt()}ms" }
}

inline fun <T, R> Array<out T>.flatGroupBy(transform: (T) -> Iterable<R>): Map<R, List<T>> {
    return flatMap { spawn -> transform.invoke(spawn).map { it to spawn } }.groupBy { it.first }.mapValues { it.value.map { it.second } }
}