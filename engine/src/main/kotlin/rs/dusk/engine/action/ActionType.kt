package rs.dusk.engine.action

import kotlinx.coroutines.CancellationException

sealed class ActionType : CancellationException() {
    object Movement : ActionType()
    object Teleport : ActionType()
    object Combat : ActionType()
    object Misc : ActionType()
    object Assisting : ActionType()
    object Global : ActionType()
    object FloorItem : ActionType()
    object Logout : ActionType()
    object Trade : ActionType()
    object Bank : ActionType()
    object Equipping : ActionType()
    object Woodcutting : ActionType()
}