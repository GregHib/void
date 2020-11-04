package rs.dusk.engine.entity.character.player

import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.entity.item.BodyPart
import rs.dusk.engine.entity.item.EquipType

class BodyParts(
    private val equipment: Container,
    private val definitions: ItemDefinitions,
    val looks: IntArray
) {
    private val parts = IntArray(13)

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
            showItem(part, item) -> definitions.get(item)["equip", -1] or 0x8000
            showBodyPart(part, item) -> looks[part.index] or 0x100
            else -> 0
        }
        return before != parts[part.ordinal]
    }

    private fun showItem(part: BodyPart, item: Int): Boolean {
        return item != -1 && when (part) {
            BodyPart.Hair, BodyPart.Beard -> false
            BodyPart.Arms -> definitions.get(item)["type", EquipType.None] != EquipType.Sleeveless
            else -> true
        }
    }

    private fun showBodyPart(part: BodyPart, item: Int): Boolean {
        val type = definitions.get(item)["type", EquipType.None]
        return part.index != -1 && when (part) {
            BodyPart.Hair -> type != EquipType.FullFace && type != EquipType.Hair
            BodyPart.Beard -> type != EquipType.FullFace && type != EquipType.Mask
            else -> true
        }
    }
}