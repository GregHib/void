package world.gregs.voidps.engine.tick.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.list.PooledMapList

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
@Deprecated("Use scripts instead")
abstract class EntityTask<T : Character> : Runnable {
    private val logger = InlineLogger()

    abstract val entities: PooledMapList<T>

    abstract fun runAsync(entity: T)

    override fun run() = runBlocking {
        coroutineScope {
            entities.forEach {
                launch(Contexts.Updating) {
                    runAsync(it)
                }
            }
        }
    }
}