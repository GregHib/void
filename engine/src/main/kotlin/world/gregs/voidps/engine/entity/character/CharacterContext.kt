package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Context of a queue or interaction action to access a [character] as either [player] or [npc] but which is only known implicitly
 */
interface CharacterContext {
    val character: Character
    val player: Player
        get() = character as Player
    val npc: NPC
        get() = character as NPC
    var onCancel: (() -> Unit)?
}