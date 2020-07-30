package rs.dusk.engine.client.update.encode

import rs.dusk.core.io.Modifier
import rs.dusk.core.io.write.Writer
import rs.dusk.engine.entity.character.update.VisualEncoder
import rs.dusk.engine.entity.character.update.visual.Hit
import rs.dusk.engine.entity.character.update.visual.Hits

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class HitsEncoder(private val npc: Boolean, mask: Int) : VisualEncoder<Hits>(mask) {

    override fun encode(writer: Writer, visual: Hits) {
        val (damage, player, other) = visual
        writer.apply {
            if (npc) {
                writeByte(damage.size, Modifier.INVERSE)
            } else {
                writeByte(damage.size)
            }
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
                if (npc) {
                    writeByte(hit.percentage)
                } else {
                    writeByte(hit.percentage, Modifier.INVERSE)
                }
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