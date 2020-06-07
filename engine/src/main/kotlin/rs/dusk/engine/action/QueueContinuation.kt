package rs.dusk.engine.action

import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.Continuation

object QueueContinuation : Continuation<Any> {

    override val context = newSingleThreadContext("ActionQueues")

    override fun resumeWith(result: Result<Any>) {

    }

}