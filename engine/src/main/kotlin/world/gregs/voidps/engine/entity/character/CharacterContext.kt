package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Context of a queue or interaction action to access a [character] as either [player] or [npc]
 */
interface CharacterContext<C : Character> {
    val character: C
    var onCancel: (() -> Unit)?
    val CharacterContext<Player>.player: Player
        get() = character
    val CharacterContext<NPC>.npc: NPC
        get() = character
}