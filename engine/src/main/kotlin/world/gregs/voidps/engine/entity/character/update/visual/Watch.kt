package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.visual.VisualMask.NPC_WATCH_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_WATCH_MASK

fun Character.watch(character: Character?) {
    val mask = if (this is Player) PLAYER_WATCH_MASK else if (this is NPC) NPC_WATCH_MASK else return
    visuals.watch.index = if (character is Player) character.index or 0x8000 else if (character is NPC) character.index else -1
    visuals.flag(mask)
}