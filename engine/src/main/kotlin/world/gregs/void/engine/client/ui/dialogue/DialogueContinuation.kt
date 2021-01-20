package world.gregs.void.engine.client.ui.dialogue

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import world.gregs.void.engine.action.Contexts
import kotlin.coroutines.Continuation

object DialogueContinuation : Continuation<Any> {

    val logger = InlineLogger()

    override val context = Contexts.Game

    override fun resumeWith(result: Result<Any>) {
        result.onFailure {
            if(it !is CancellationException) {
                logger.error(it) { "Error in dialogue" }
            }
        }
    }

}