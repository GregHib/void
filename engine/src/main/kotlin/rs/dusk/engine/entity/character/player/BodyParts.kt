package rs.dusk.engine.entity.character.player

import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.item.BodyPart
import rs.dusk.engine.entity.item.EquipSlot
import rs.dusk.engine.entity.item.EquipType
import rs.dusk.engine.entity.item.detail.ItemDetails

class BodyParts(
    private val equipment: Container,
    val looks: IntArray,
    private val details: ItemDetails,
    private val decoder: ItemDecoder
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
            BodyPart.Cape -> updated = updated or update(BodyPart.Hat)
            else -> {
            }
        }
        return updated
    }

    fun update(part: BodyPart): Boolean {
        val item = equipment.getItem(part.slot.index)
        val before = parts[part.ordinal]
        parts[part.ordinal] = when {
            showItem(part, item) -> details.get(item).equip or 0x8000
            showBodyPart(part) -> looks[part.index] or 0x100
            else -> 0
        }
        return before != parts[part.ordinal]
    }

    private fun showItem(part: BodyPart, item: Int): Boolean {
        return item != -1 && when (part) {
            BodyPart.Arms -> fullBody(item)
            BodyPart.Hat -> !hideHair(item)
            BodyPart.Beard -> !hideBeard(item)
            else -> true
        }
    }

    private fun showBodyPart(part: BodyPart): Boolean {
        return part.index != -1 &&
                if (part == BodyPart.Hair) !hoodedCape(equipment.getItem(EquipSlot.Cape.index)) else true
    }

    private fun hideHair(item: Int): Boolean {
        val def = decoder.getSafe(item)
        val name = def.name
        val type = details.get(item).type
        return type == EquipType.Hair || (type == EquipType.Mask && !name.contains("beard"))
    }

    private fun hoodedCape(item: Int): Boolean {
        return details.get(item).type == EquipType.HoodedCape
    }

    private fun fullBody(item: Int): Boolean {
        return details.get(item).type == EquipType.FullBody
    }

    private fun hideBeard(item: Int): Boolean {
        val def = decoder.getSafe(item)
        val name = def.name.toLowerCase()
        return hideHair(item)
                && !name.contains("horns")
                && !name.contains("hat")
                && !name.contains("afro")
                && name != "leather cowl"
                && !name.contains("headdress")
                && !name.contains("hood")
                && !isMask(name)
                && !isHelm(name)
    }

    private fun isHelm(name: String) = name.contains("helm") && !isHelmException(name)

    private fun isHelmException(name: String) =
        isFullHelm(name) || name.contains("decorative") || name.contains("verac") || name.contains("guthan") || name.contains(
            "fishbowl"
        ) || name.contains("heraldic") || name.contains("lunar") || name.contains("tyras") || name.contains("slayer") || name.contains(
            "cyclopean"
        ) || name.contains("wildstalker") || name.contains("trickster") || name.contains("vanguard")

    private fun isFullHelm(name: String) = name.contains("full") && !isFullHelmException(name)

    private fun isFullHelmException(name: String) = name.contains("third-age") || name.contains("statius")

    private fun isMask(name: String) = name.contains("mask") && !isMaskException(name)

    private fun isMaskException(name: String) =
        name.contains("h'ween") || name.contains("mime") || name.contains("frog") || name.contains("virtus") || name.contains(
            "gorilla"
        )

}