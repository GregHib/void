package rs.dusk.engine.entity.character.update.visual

import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCEvent
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.entity.character.update.Visual
import rs.dusk.engine.entity.character.update.Visuals

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Graphic(
    var id: Int = -1,
    var delay: Int = 0,
    var height: Int = 0,
    var rotation: Int = 0,
    var forceRefresh: Boolean = false
) : Visual {
    override fun reset(character: Character) {
        id = -1
        delay = 0
        height = 0
        rotation = 0
        forceRefresh = false
    }
}

const val PLAYER_GRAPHIC_0_MASK = 0x2
const val PLAYER_GRAPHIC_1_MASK = 0x100
const val PLAYER_GRAPHIC_2_MASK = 0x40000
const val PLAYER_GRAPHIC_3_MASK = 0x80000

private fun getPlayerMask(index: Int) = when (index) {
    1 -> PLAYER_GRAPHIC_1_MASK
    2 -> PLAYER_GRAPHIC_2_MASK
    3 -> PLAYER_GRAPHIC_3_MASK
    else -> PLAYER_GRAPHIC_0_MASK
}

const val NPC_GRAPHIC_0_MASK = 0x4
const val NPC_GRAPHIC_1_MASK = 0x1000
const val NPC_GRAPHIC_2_MASK = 0x100000
const val NPC_GRAPHIC_3_MASK = 0x20000

private fun getNPCMask(index: Int) = when (index) {
    1 -> NPC_GRAPHIC_1_MASK
    2 -> NPC_GRAPHIC_2_MASK
    3 -> NPC_GRAPHIC_3_MASK
    else -> NPC_GRAPHIC_0_MASK
}

fun Player.flagGraphic(index: Int) = visuals.flag(getPlayerMask(index))

fun NPC.flagGraphic(index: Int) = visuals.flag(getNPCMask(index))

fun Player.getGraphic(index: Int = 0) = visuals.getOrPut(getPlayerMask(index)) { Graphic() }

fun NPC.getGraphic(index: Int = 0) = visuals.getOrPut(getNPCMask(index)) { Graphic() }

private fun Visuals.getIndex(indexer: (Int) -> Int): Int {
    for (i in 0 until 4) {
        if (!flagged(indexer(i))) {
            return i
        }
    }
    return -1
}

fun PlayerEvent.graphic(id: Int, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) = player.setGraphic(id, delay, height, rotation, forceRefresh)

fun NPCEvent.graphic(id: Int, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) = npc.setGraphic(id, delay, height, rotation, forceRefresh)

fun Player.setGraphic(id: Int, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) {
    val index = visuals.getIndex(::getPlayerMask)
    setGraphic(getGraphic(index), id, delay, height, rotation, forceRefresh)
    flagGraphic(index)
}

fun NPC.setGraphic(id: Int, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) {
    val index = visuals.getIndex(::getNPCMask)
    setGraphic(getGraphic(index), id, delay, height, rotation, forceRefresh)
    flagGraphic(index)
}

private fun setGraphic(graphic: Graphic, id: Int, delay: Int, height: Int, rotation: Int, forceRefresh: Boolean) {
    graphic.id = id
    graphic.delay = delay
    graphic.height = height
    graphic.rotation = rotation
    graphic.forceRefresh = forceRefresh
}