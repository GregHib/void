package world.gregs.voidps.engine.entity.character.player.equip

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.Body
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyColour
import world.gregs.voidps.network.login.protocol.visual.update.player.BodyPart

/**
 * Keeps track of what outfit style [looks] and which equipped items [parts] should be shown
 */
data class BodyParts(
    override var male: Boolean = true,
    val looks: IntArray = if (male) DEFAULT_LOOK_MALE.clone() else DEFAULT_LOOK_FEMALE.clone(),
    val colours: IntArray = DEFAULT_COLOURS.clone()
) : Body {
    private val parts = IntArray(12)

    private lateinit var equipment: Inventory

    fun link(equipment: Inventory) {
        this.equipment = equipment
    }

    fun updateAll() {
        BodyPart.entries.forEach {
            updateConnected(it)
        }
    }

    override fun getColour(index: Int) = colours.getOrNull(index) ?: 0

    override fun getLook(index: Int) = looks.getOrNull(index) ?: 0

    override fun get(index: Int) = parts.getOrNull(index) ?: 0

    override fun setLook(part: BodyPart, value: Int) {
        if (part.index == -1) {
            return
        }
        looks[part.index] = value
        updateConnected(part)
    }

    override fun setColour(part: BodyColour, value: Int) {
        if (part.index == -1) {
            return
        }
        colours[part.index] = value
    }

    override fun updateConnected(part: BodyPart, skip: Boolean): Boolean {
        var updated = update(part, skip)
        when (part) {
            BodyPart.Chest -> updated = updated or update(BodyPart.Arms, skip)
            BodyPart.Head -> {
                updated = updated or update(BodyPart.Hair, skip)
                updated = updated or update(BodyPart.Beard, skip)
            }
            else -> {
            }
        }
        return updated
    }

    fun update(part: BodyPart, skip: Boolean): Boolean {
        val item = if (skip) Item.EMPTY else equipment[part.slot.index]
        val before = parts[part.ordinal]
        parts[part.ordinal] = when {
            showItem(part, item) -> if (item.def.equipIndex != -1) item.def.equipIndex or 0x8000 else 0
            showBodyPart(part, item) -> looks[part.index] + 0x100
            !skip && showDefault(part) ->
                (if (male) DEFAULT_LOOK_MALE else DEFAULT_LOOK_FEMALE)[part.index] + 0x100
            else -> 0
        }
        return before != parts[part.ordinal]
    }

    private fun showDefault(part: BodyPart): Boolean {
        if (part != BodyPart.Arms) {
            return false
        }
        val chest = equipment[BodyPart.Chest.slot.index]
        if (chest.isEmpty() || chest.type != EquipType.Sleeveless) {
            return false
        }
        return part.index != -1 && looks[part.index] < 0
    }

    private fun showItem(part: BodyPart, item: Item): Boolean {
        return item.isNotEmpty() && when (part) {
            BodyPart.Hair, BodyPart.Beard -> false
            BodyPart.Arms -> item.type != EquipType.Sleeveless
            else -> true
        }
    }

    /**
     * Don't show hair for EquipType.Hair, jaws for Mask's, and both for FullFace coverings.
     */
    private fun showBodyPart(part: BodyPart, item: Item): Boolean {
        val type = item.type
        return part.index != -1 && looks[part.index] >= 0 && when (part) {
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
        if (!looks.contentEquals(other.looks)) return false
        if (!colours.contentEquals(other.colours)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = male.hashCode()
        result = 31 * result + looks.contentHashCode()
        result = 31 * result + colours.contentHashCode()
        return result
    }

    companion object {
        val DEFAULT_LOOK_MALE = intArrayOf(3, 14, 18, 26, 34, 38, 42)
        val DEFAULT_LOOK_FEMALE = intArrayOf(46, -1, 58, 61, 68, 72, 80)
        val DEFAULT_COLOURS = IntArray(5)
    }
}