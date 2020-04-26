package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Watch(var index: Int = -1) : Visual

fun Player.flagWatch() = visuals.flag(0x10)

fun NPC.flagWatch() = visuals.flag(0x1)

fun Indexed.flagWatch() {
    if (this is Player) flagWatch() else if (this is NPC) flagWatch()
}

fun Indexed.getWatch() = visuals.getOrPut(Watch::class) { Watch() }

fun NPC.watch(npc: NPC) = setWatch(npc.index)

fun Player.watch(player: Player) = setWatch(player.index or 0x8000)

fun Indexed.setWatch(targetIndex: Int) {
    val watch = getWatch()
    watch.index = targetIndex
    flagWatch()
}
