package world.gregs.voidps.engine.entity.character.npc

sealed class NPCMoveType {
    data object None : NPCMoveType()
    data object Crawl : NPCMoveType()
    data object Walk : NPCMoveType()
    data object Run : NPCMoveType()
    data object Teleport : NPCMoveType()
}
