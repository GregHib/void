package world.gregs.void.engine.entity.character.npc

/**
 * @author GregHib <greg@gregs.world>
 * @since May 15, 2020
 */
sealed class NPCMoveType {
    object None : NPCMoveType()
    object Crawl : NPCMoveType()
    object Walk : NPCMoveType()
    object Run : NPCMoveType()
    object Teleport : NPCMoveType()
}