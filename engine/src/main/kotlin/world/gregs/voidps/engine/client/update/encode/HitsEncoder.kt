package world.gregs.voidps.engine.client.update.encode

import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.Hits

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class HitsEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<Hits>(mask) {

    override fun encode(writer: Writer, visual: Hits) {
        val (damage, player, other) = visual
        writer.apply {
            writeByte(damage.size, if (npc) Modifier.SUBTRACT else Modifier.INVERSE)
            damage.forEach { hit ->
                if (hit.amount == 0 && !interactingWith(player, other, hit.source)) {
                    writeSmart(32766)
                    return@forEach
                }

                val mark = getMarkId(player, other, hit)

                if (hit.soak != -1) {
                    writeSmart(32767)
                }

                writeSmart(mark)
                writeSmart(hit.amount)

                if (hit.soak != -1) {
                    writeSmart(Hit.Mark.Absorb.id)
                    writeSmart(hit.soak)
                }

                writeSmart(hit.delay)
                writeByte(hit.percentage, if (npc) Modifier.NONE else Modifier.ADD)
            }
        }
    }

    private fun getMarkId(player: Int, other: Int, marker: Hit): Int {
        if (marker.mark == Hit.Mark.Healed) {
            return marker.mark.id
        }

        if (marker.amount == 0) {
            return Hit.Mark.Missed.id
        }

        var mark = marker.mark.id

        if (marker.critical) {
            mark += 10
        }

        if (!interactingWith(player, other, marker.source)) {
            mark += 14
        }

        return mark
    }

    private fun interactingWith(player: Int, victim: Int, source: Int): Boolean {
        return player == victim || player == source
    }

}