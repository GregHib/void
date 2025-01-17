package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Context of an event, queue or action to access a [character] as either [player] or [npc]
 */
interface CharacterContext<C : Character> {
    val character: C
    var onCancel: (() -> Unit)?
    val CharacterContext<Player>.player: Player
        get() = character
    val CharacterContext<NPC>.npc: NPC
        get() = character
}