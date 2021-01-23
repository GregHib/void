package world.gregs.voidps.engine.client.update.encode.npc

import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.npc.COMBAT_LEVEL_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.CombatLevel

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class CombatLevelEncoder : VisualEncoder<CombatLevel>(COMBAT_LEVEL_MASK) {

    override fun encode(writer: Writer, visual: CombatLevel) {
        writer.writeShort(visual.level, order = Endian.LITTLE)
    }

}