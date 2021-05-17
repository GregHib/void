package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.utility.func.toInt
import world.gregs.voidps.utility.get

data class Graphic(
    var id: Int = -1,
    var delay: Int = 0,
    var height: Int = 0,
    var rotation: Int = 0,
    var forceRefresh: Boolean = false,
    var slot: Int = 0
) : Visual {

    val packedDelayHeight: Int
        get() = (delay and 0xffff) or (height shl 16)
    val packedRotationRefresh: Int
        get() = (rotation and 0x7) or (slot shl 3) or (forceRefresh.toInt() shl 7)

    override fun needsReset(character: Character): Boolean {
        return id != -1
    }

    override fun reset(character: Character) {
        id = -1
        delay = 0
        height = 0
        rotation = 0
        forceRefresh = false
    }
}

const val PLAYER_GRAPHIC_0_MASK = 0x20
const val PLAYER_GRAPHIC_1_MASK = 0x200
const val PLAYER_GRAPHIC_2_MASK = 0x40000
const val PLAYER_GRAPHIC_3_MASK = 0x80000

private fun getPlayerMask(index: Int) = when (index) {
    1 -> PLAYER_GRAPHIC_1_MASK
    2 -> PLAYER_GRAPHIC_2_MASK
    3 -> PLAYER_GRAPHIC_3_MASK
    else -> PLAYER_GRAPHIC_0_MASK
}

const val NPC_GRAPHIC_0_MASK = 0x20
const val NPC_GRAPHIC_1_MASK = 0x400
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

fun Player.clearGraphic() = setGraphic(-1)

fun NPC.clearGraphic() = setGraphic(-1)

fun Player.setGraphic(name: String, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) {
    setGraphic(get<GraphicDefinitions>().getIdOrNull(name) ?: return, delay, height, rotation, forceRefresh)
}

fun NPC.setGraphic(name: String, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) {
    setGraphic(get<AnimationDefinitions>().getIdOrNull(name) ?: return, delay, height, rotation, forceRefresh)
}

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