package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.visual.VisualMask.NPC_GRAPHIC_1_MASK
import world.gregs.voidps.network.visual.VisualMask.NPC_GRAPHIC_2_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_GRAPHIC_1_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_GRAPHIC_2_MASK
import world.gregs.voidps.network.visual.Visuals

private fun getPlayerMask(index: Int) = when (index) {
    1 -> PLAYER_GRAPHIC_2_MASK
    else -> PLAYER_GRAPHIC_1_MASK
}

private fun getNPCMask(index: Int) = when (index) {
    1 -> NPC_GRAPHIC_2_MASK
    else -> NPC_GRAPHIC_1_MASK
}

private fun mask(character: Character, index: Int) = if (character is Player) getPlayerMask(index) else getNPCMask(index)

private fun index(character: Character) = if (character is Player) character.visuals.getIndex(::getPlayerMask) else character.visuals.getIndex(::getNPCMask)

fun Character.flagGraphic(index: Int) = visuals.flag(mask(this, index))

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
    val graphic = if (index == 0) visuals.primaryGraphic else visuals.secondaryGraphic
    graphic.reset()
    flagGraphic(index)
}

fun Character.setGraphic(id: String, delay: Int? = null) {
    val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
    val index = index(this)
    val graphic = if (index == 0) visuals.primaryGraphic else visuals.secondaryGraphic
    graphic.id = definition.id
    graphic.delay = delay ?: definition["delay", 0]
    val characterHeight = (this as? NPC)?.def?.get("height", 0) ?: 40
    graphic.height = (characterHeight + definition["height", -1000]).coerceAtLeast(0)
    graphic.rotation = definition["rotation", 0]
    graphic.forceRefresh = definition["force_refresh", false]
    flagGraphic(index)
}