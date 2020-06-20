package rs.dusk.engine.action

import kotlin.coroutines.Continuation

object ActionContinuation : Continuation<Any> {

    override val context = Contexts.Engine

    override fun resumeWith(result: Result<Any>) {

    }

}