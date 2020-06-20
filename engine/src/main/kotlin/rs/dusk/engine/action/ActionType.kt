package rs.dusk.engine.action

import kotlinx.coroutines.CancellationException

sealed class ActionType : CancellationException() {
    object Movement : ActionType()
    object Teleport : ActionType()
    object Combat : ActionType()
    object Misc : ActionType()
    object Global : ActionType()
}