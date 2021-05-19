package world.gregs.voidps.engine.tick.task

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.list.PooledMapList

@Deprecated("Use scripts instead")
abstract class EntityTask<T : Character>(val sequential: Boolean = false) : Runnable {
    private val logger = InlineLogger()

    abstract val entities: PooledMapList<T>

    open fun predicate(entity: T): Boolean = true

    abstract fun runAsync(entity: T)

    override fun run() = runBlocking {
        if (sequential) {
            entities.forEach {
                if (predicate(it)) {
                    runAsync(it)
                }
            }
        } else {
            coroutineScope {
                entities.forEach {
                    if (predicate(it)) {
                        launch(Contexts.Updating) {
                            runAsync(it)
                        }
                    }
                }
            }
        }
    }
}