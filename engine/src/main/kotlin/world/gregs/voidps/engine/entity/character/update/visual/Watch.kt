package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class Watch(var index: Int = -1) : Visual

const val PLAYER_WATCH_MASK = 0x1
const val NPC_WATCH_MASK = 0x80

fun Character.watch(character: Character?) {
    val mask = if (this is Player) PLAYER_WATCH_MASK else if (this is NPC) NPC_WATCH_MASK else return
    val watch = visuals.getOrPut(mask) { Watch() }
    watch.index = if (character is Player) character.index or 0x8000 else if (character is NPC) character.index else -1
    visuals.flag(mask)
}