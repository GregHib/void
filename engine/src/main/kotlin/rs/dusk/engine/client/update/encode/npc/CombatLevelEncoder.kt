package rs.dusk.engine.client.update.encode.npc

import rs.dusk.core.io.Endian
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.npc.COMBAT_LEVEL_MASK
import rs.dusk.engine.entity.character.update.visual.npc.CombatLevel

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class CombatLevelEncoder : VisualEncoder<CombatLevel>(COMBAT_LEVEL_MASK) {

    override fun encode(writer: Writer, visual: CombatLevel) {
        writer.writeShort(visual.level, order = Endian.LITTLE)
    }

}