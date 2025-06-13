package world.gregs.voidps.engine.client.update.iterator

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue
import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.entity.character.Character
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ParallelIterator<C : Character> : TaskIterator<C> {
    private val queue = ObjectArrayFIFOQueue<Future<*>>()
    private val logger = InlineLogger()

    override fun run(task: CharacterTask<C>) {
        for (character in task.characters) {
            if (task.predicate(character)) {
                queue.enqueue(
                    executor.submit {
                        try {
                            task.run(character)
                        } catch (t: Throwable) {
                            logger.warn(t) { "Exception in parallel task." }
                        }
                    },
                )
            }
        }
        while (!queue.isEmpty) {
            queue.dequeue().get()
        }
    }

    companion object {
        private val executor = Executors.newCachedThreadPool()
    }
}
