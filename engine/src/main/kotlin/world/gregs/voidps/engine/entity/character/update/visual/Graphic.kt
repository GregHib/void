package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.utility.func.toInt
import world.gregs.voidps.utility.get

/**
 * @param id graphic id
 * @param delay delay to start graphic 30 = 1 tick
 * @param height 0..255 start height off the ground
 * @param rotation 0..7
 */
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

private fun getPlayerMask(index: Int) = when (index) {
    1 -> PLAYER_GRAPHIC_1_MASK
    else -> PLAYER_GRAPHIC_0_MASK
}

const val NPC_GRAPHIC_0_MASK = 0x20
const val NPC_GRAPHIC_1_MASK = 0x400

private fun getNPCMask(index: Int) = when (index) {
    1 -> NPC_GRAPHIC_1_MASK
    else -> NPC_GRAPHIC_0_MASK
}

private fun mask(character: Character, index: Int) = if (character is Player) getPlayerMask(index) else getNPCMask(index)

private fun index(character: Character) = if (character is Player) character.visuals.getIndex(::getPlayerMask) else character.visuals.getIndex(::getNPCMask)

fun Character.flagGraphic(index: Int) = visuals.flag(mask(this, index))

fun Character.getGraphic(index: Int = 0) = visuals.getOrPut(mask(this, index)) { Graphic() }

private fun Visuals.getIndex(indexer: (Int) -> Int): Int {
    for (i in 0 until 2) {
        if (!flagged(indexer(i))) {
            return i
        }
    }
    return -1
}

fun Character.clearGraphic() {
    val index = index(this)
    getGraphic(index).reset(this)
    flagGraphic(index)
}

fun Character.setGraphic(name: String, delay: Int? = null) {
    val definition = get<GraphicDefinitions>().getOrNull(name) ?: return
    val characterHeight = (this as? NPC)?.def?.get("height", 0) ?: 40
    setGraphic(definition.id, delay ?: definition["delay", 0], (characterHeight + definition["height", -1000]).coerceAtLeast(0), definition["rotation", 0], definition["force_refresh", false])
}

fun Character.setGraphic(id: Int, delay: Int = 0, height: Int = 0, rotation: Int = 0, forceRefresh: Boolean = false) {
    val index = index(this)
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