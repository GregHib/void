package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.item.BodyPart
import world.gregs.voidps.engine.entity.item.EquipType
import world.gregs.voidps.engine.entity.item.Item

class BodyParts(
    private val equipment: Container,
    val looks: IntArray
) {
    private val parts = IntArray(12)

    fun get(index: Int) = parts.getOrNull(index) ?: -1

    fun updateConnected(part: BodyPart): Boolean {
        var updated = update(part)
        when (part) {
            BodyPart.Chest -> updated = updated or update(BodyPart.Arms)
            BodyPart.Hat -> {
                updated = updated or update(BodyPart.Hair)
                updated = updated or update(BodyPart.Beard)
            }
            else -> {
            }
        }
        return updated
    }

    fun update(part: BodyPart): Boolean {
        val item = equipment.getItem(part.slot.index)
        val before = parts[part.ordinal]
        parts[part.ordinal] = when {
            showItem(part, item) -> item.def["equip", -1] or 0x8000
            showBodyPart(part, item) -> looks[part.index] or 0x100
            else -> 0
        }
        return before != parts[part.ordinal]
    }

    private fun showItem(part: BodyPart, item: Item): Boolean {
        return item.isNotEmpty() && when (part) {
            BodyPart.Hair, BodyPart.Beard -> false
            BodyPart.Arms -> item.def["type", EquipType.None] != EquipType.Sleeveless
            else -> true
        }
    }

    private fun showBodyPart(part: BodyPart, item: Item): Boolean {
        val type = item.def["type", EquipType.None]
        return part.index != -1 && when (part) {
            BodyPart.Hair -> type != EquipType.FullFace && type != EquipType.Hair
            BodyPart.Beard -> type != EquipType.FullFace && type != EquipType.Mask
            else -> true
        }
    }
}