package world.gregs.voidps.network.visual.update

import world.gregs.voidps.buffer.write.Writer

data class Hitsplat(
    val amount: Int,
    val mark: Mark,
    val percentage: Int,
    val delay: Int = 0,
    val critical: Boolean = false,
    val source: Int = -1,
    val soak: Int = -1
) {

    sealed class Mark(val id: Int) {
        object Melee : Mark(0)
        object Range : Mark(1)
        object Magic : Mark(2)
        object Regular : Mark(3)
        object Reflected : Mark(4)
        object Absorb : Mark(5)
        object Poison : Mark(6)
        object Diseased : Mark(7)
        object Missed : Mark(8)
        object Healed : Mark(9)
        object Cannon : Mark(13)
    }

    fun write(writer: Writer, observer: Int, victim: Int, add: Boolean) {
        if (amount == 0 && !interactingWith(observer, victim, source)) {
            writer.writeSmart(32766)
        } else {
            val mark = getMarkId(observer, victim)

            if (soak != -1) {
                writer.writeSmart(32767)
            }

            writer.writeSmart(mark)
            writer.writeSmart(amount)

            if (soak != -1) {
                writer.writeSmart(Mark.Absorb.id)
                writer.writeSmart(soak)
            }
        }
        writer.writeSmart(delay)
        if (add) {
            writer.writeByteAdd(percentage)
        } else {
            writer.writeByte(percentage)
        }
    }

    private fun getMarkId(observer: Int, victim: Int): Int {
        if (mark == Mark.Healed) {
            return mark.id
        }

        if (amount == 0) {
            return Mark.Missed.id
        }

        var mark = mark.id

        if (critical) {
            mark += 10
        }

        if (!interactingWith(observer, victim, source)) {
            mark += 14
        }

        return mark
    }

    private fun interactingWith(observer: Int, victim: Int, source: Int): Boolean {
        return observer == victim || observer == source
    }
}