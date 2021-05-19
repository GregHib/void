package world.gregs.voidps.engine.entity.character.npc

sealed class NPCMoveType {
    object None : NPCMoveType()
    object Crawl : NPCMoveType()
    object Walk : NPCMoveType()
    object Run : NPCMoveType()
    object Teleport : NPCMoveType()
}