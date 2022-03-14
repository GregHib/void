package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.network.visual.BodyPart
import world.gregs.voidps.network.visual.update.Looks

/**
 * Keeps track of what outfit style [looks] and which equipped items [parts] should be shown
 */
class BodyParts(
    private val equipment: Container,
    override var male: Boolean,
    override val looks: IntArray = if (male) DEFAULT_LOOK_MALE.clone() else DEFAULT_LOOK_FEMALE.clone()
) : Looks {
    private val parts = IntArray(12)

    override fun get(index: Int) = parts.getOrNull(index) ?: 0

    override fun updateConnected(part: BodyPart, skip: Boolean): Boolean {
        var updated = update(part, skip)
        when (part) {
            BodyPart.Chest -> updated = updated or update(BodyPart.Arms, skip)
            BodyPart.Hat -> {
                updated = updated or update(BodyPart.Hair, skip)
                updated = updated or update(BodyPart.Beard, skip)
            }
            else -> {
            }
        }
        return updated
    }

    fun update(part: BodyPart, skip: Boolean): Boolean {
        val item = if (skip) Item.EMPTY else equipment.getItem(part.slot.index)
        val before = parts[part.ordinal]
        parts[part.ordinal] = when {
            showItem(part, item) -> if (item.def.has("equip")) item.def["equip", -1] or 0x8000 else 0
            showBodyPart(part, item) -> looks[part.index] + 0x100
            else -> 0
        }
        return before != parts[part.ordinal]
    }

    private fun showItem(part: BodyPart, item: Item): Boolean {
        return item.isNotEmpty() && when (part) {
            BodyPart.Hair, BodyPart.Beard -> false
            BodyPart.Arms -> item.type != EquipType.Sleeveless
            else -> true
        }
    }

    private fun showBodyPart(part: BodyPart, item: Item): Boolean {
        val type = item.type
        return part.index != -1 && looks[part.index] > 0 && when (part) {
            BodyPart.Hair -> type != EquipType.FullFace && type != EquipType.Hair
            BodyPart.Beard -> type != EquipType.FullFace && type != EquipType.Mask
            else -> true
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BodyParts

        if (male != other.male) return false
        if (!parts.contentEquals(other.parts)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = male.hashCode()
        result = 31 * result + parts.contentHashCode()
        return result
    }


    companion object {
        val DEFAULT_LOOK_MALE = intArrayOf(3, 14, 18, 26, 34, 38, 42)
        val DEFAULT_LOOK_FEMALE = intArrayOf(46, -1, 58, 61, 68, 72, 80)
    }
}