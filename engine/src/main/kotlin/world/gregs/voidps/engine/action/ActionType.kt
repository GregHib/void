package world.gregs.voidps.engine.action

import kotlinx.coroutines.CancellationException

sealed class ActionType : CancellationException() {
    object None : ActionType()
    object Climb : ActionType()
    object Follow : ActionType()
    object Teleport : ActionType()
    object Movement : ActionType()
    object Combat : ActionType()
    object Misc : ActionType()
    object Assisting : ActionType()
    object FloorItem : ActionType()
    object Logout : ActionType()
    object Trade : ActionType()
    object Bank : ActionType()
    object Equipping : ActionType()
    object Woodcutting : ActionType()
    object Mining : ActionType()
    object Prospecting : ActionType()
    object Fishing : ActionType()
    object FireMaking : ActionType()
    object Shopping : ActionType()
    object Resting : ActionType()
    object Emote : ActionType()
    object OpenDoor : ActionType()
    object Dying : ActionType()
    object Burying : ActionType()
    object Cooking : ActionType()
    object Making : ActionType()
    object Filling : ActionType()
}