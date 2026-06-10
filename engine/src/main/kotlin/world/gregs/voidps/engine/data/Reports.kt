package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Saves abuse reports off the game thread, falling back to [fallback] storage on failure
 */
class Reports(
    private val storage: Storage,
    private val fallback: Storage = storage,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    private val logger = InlineLogger()

    private val fallbackHandler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Fallback report save failed!" }
    }

    fun queue(report: AbuseReport) {
        if (Settings["storage.disabled", false]) {
            return
        }
        val handler = CoroutineExceptionHandler { _, exception ->
            logger.error(exception) { "Error saving abuse report!" }
            scope.launch(fallbackHandler) {
                withContext(NonCancellable) {
                    fallback.saveReport(report)
                }
            }
        }
        scope.launch(handler) {
            withContext(NonCancellable) {
                storage.saveReport(report)
            }
        }
    }
}
