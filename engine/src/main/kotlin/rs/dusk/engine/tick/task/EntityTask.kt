package rs.dusk.engine.tick.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.list.PooledMapList
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
@Deprecated("Use scripts instead")
abstract class EntityTask<T : Character>(priority: Int) : EngineTask(priority) {
    private val logger = InlineLogger()

    abstract val entities: PooledMapList<T>

    abstract fun runAsync(entity: T)

    override fun run() = runBlocking {
        val took = measureTimeMillis {
            coroutineScope {
                entities.forEach {
                    launch(Contexts.Updating) {
                        runAsync(it)
                    }
                }
            }
        }
        if (took >= 5) {
            logger.info { "${this@EntityTask::class.simpleName} took ${took}ms" }
        }
    }
}