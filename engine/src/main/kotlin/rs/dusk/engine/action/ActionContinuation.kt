package rs.dusk.engine.action

import com.github.michaelbull.logging.InlineLogger
import kotlin.coroutines.Continuation

object ActionContinuation : Continuation<Any> {

    val logger = InlineLogger()

    override val context = Contexts.Engine

    override fun resumeWith(result: Result<Any>) {
        result.onFailure {
            logger.error(it) { "Error in action" }
        }
    }

}