package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Graphic
import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Graphics(val graphics: Array<Graphic?> = arrayOfNulls(4)) : Visual {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graphics

        if (!graphics.contentEquals(other.graphics)) return false

        return true
    }

    override fun hashCode(): Int {
        return graphics.contentHashCode()
    }
}

fun Player.flagGraphics(index: Int) = visuals.flag(
    when (index) {
        1 -> 0x100
        2 -> 0x40000
        3 -> 0x80000
        else -> 0x2
    }
)

fun NPC.flagGraphics(index: Int) = visuals.flag(
    when (index) {
        1 -> 0x1000
        2 -> 0x100000
        3 -> 0x20000
        else -> 0x4
    }
)

fun Indexed.flagGraphics(index: Int) {
    if (this is Player) flagGraphics(index) else if (this is NPC) flagGraphics(index)
}

fun Indexed.getGraphics() = visuals.getOrPut(Graphics::class) { Graphics() }

fun Indexed.setGraphic(graphic: Graphic) {
    val graphics = getGraphics().graphics
    for (i in graphics.indices) {
        if (graphics[i] == null) {
            graphics[i] = graphic
            graphic.index = i + 1
            flagGraphics(i)
            break
        }
    }
}
