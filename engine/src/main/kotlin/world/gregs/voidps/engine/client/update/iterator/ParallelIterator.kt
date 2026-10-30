package world.gregs.voidps.engine.client.update.iterator

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.entity.character.Character
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Runs a [CharacterTask] for all characters across a fixed number of worker threads,
 * each worker taking the next character until there are none left.
 */
class ParallelIterator<C : Character> : TaskIterator<C> {
    private val characters = ArrayList<C>()
    private val futures = ArrayList<Future<*>>(THREADS)
    private val next = AtomicInteger()
    private val logger = InlineLogger()

    override fun run(task: CharacterTask<C>) {
        for (character in task.characters) {
            if (task.predicate(character)) {
                characters.add(character)
            }
        }
        next.set(0)
        try {
            repeat(minOf(THREADS, characters.size)) {
                futures.add(executor.submit { process(task) })
            }
            for (future in futures) {
                future.get()
            }
        } finally {
            futures.clear()
            characters.clear()
        }
    }

    private fun process(task: CharacterTask<C>) {
        while (true) {
            val index = next.getAndIncrement()
            if (index >= characters.size) {
                return
            }
            try {
                task.run(characters[index])
            } catch (t: Throwable) {
                logger.warn(t) { "Exception in parallel task." }
            }
        }
    }

    companion object {
        private val THREADS = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        private val count = AtomicInteger()
        private val executor = Executors.newFixedThreadPool(THREADS) { runnable ->
            thread(start = false, isDaemon = true, name = "parallel-update-${count.incrementAndGet()}") { runnable.run() }
        }
    }
}
