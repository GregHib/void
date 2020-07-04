package rs.dusk.engine.model.entity.index.update.visual

import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCEvent
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent
import rs.dusk.engine.model.entity.index.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Watch(var index: Int = -1) : Visual

const val PLAYER_WATCH_MASK = 0x10

const val NPC_WATCH_MASK = 0x1

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