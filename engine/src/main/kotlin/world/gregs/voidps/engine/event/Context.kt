package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Context of an event, queue or action to access a [character] as either [player] or [npc]
 */
interface Context<C : Character> {
    val character: C
    var onCancel: (() -> Unit)?
    val Context<Player>.player: Player
        get() = character
    val Context<NPC>.npc: NPC
        get() = character
}