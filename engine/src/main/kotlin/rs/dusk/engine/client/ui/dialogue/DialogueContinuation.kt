package rs.dusk.engine.client.ui.dialogue

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellationException
import rs.dusk.engine.action.Contexts
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