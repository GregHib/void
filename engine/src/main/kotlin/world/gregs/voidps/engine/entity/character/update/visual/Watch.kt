package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCEvent
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.entity.character.update.Visual

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class Watch(var index: Int = -1) : Visual

const val PLAYER_WATCH_MASK = 0x1

const val NPC_WATCH_MASK = 0x80

fun Player.flagWatch() = visuals.flag(PLAYER_WATCH_MASK)

fun NPC.flagWatch() = visuals.flag(NPC_WATCH_MASK)

fun Player.getWatch() = visuals.getOrPut(PLAYER_WATCH_MASK) { Watch() }

fun NPC.getWatch() = visuals.getOrPut(NPC_WATCH_MASK) { Watch() }

fun PlayerEvent.watch(character: Character?) = this.player.watch(character)

fun NPCEvent.watch(character: Character?) = npc.watch(character)

fun NPC.watch(character: Character?) =
    setWatch(if (character is Player) character.index or 0x8000 else if (character is NPC) character.index else -1)

fun Player.watch(character: Character?) =
    setWatch(if (character is Player) character.index or 0x8000 else if (character is NPC) character.index else -1)

fun Player.setWatch(targetIndex: Int = -1) {
    getWatch().index = targetIndex
    flagWatch()
}

fun NPC.setWatch(targetIndex: Int = -1) {
    getWatch().index = targetIndex
    flagWatch()
}