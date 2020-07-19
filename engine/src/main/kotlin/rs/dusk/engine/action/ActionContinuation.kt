package rs.dusk.engine.action

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.Continuation

object ActionContinuation : Continuation<Any> {

    val logger = InlineLogger()

    override val context = Contexts.Game

    override fun resumeWith(result: Result<Any>) {
        result.onFailure {
            if(it !is CancellationException) {
                logger.error(it) { "Error in action" }
            }
        }
    }

}