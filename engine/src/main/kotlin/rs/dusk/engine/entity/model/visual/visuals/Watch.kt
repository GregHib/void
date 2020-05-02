package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

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

fun NPC.watch(player: Player?) = setWatch(if (player == null) -1 else player.index or 0x8000)

fun Player.watch(player: Player?) = setWatch(if (player == null) -1 else player.index or 0x8000)

fun Player.setWatch(targetIndex: Int = -1) {
    getWatch().index = targetIndex
    flagWatch()
}

fun NPC.setWatch(targetIndex: Int = -1) {
    getWatch().index = targetIndex
    flagWatch()
}