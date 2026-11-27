package content.bot.behaviour.loadout

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.entity.item.type
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * An item's equip information cached to avoid param lookups when selecting gear.
 */
data class GearItem(
    val id: String,
    val slot: EquipSlot,
    val twoHanded: Boolean,
    val requirements: List<Pair<Skill, Int>>,
) {
    fun canEquip(player: Player): Boolean {
        for ((skill, level) in requirements) {
            if (player.levels.getMax(skill) < level) {
                return false
            }
        }
        return true
    }

    companion object {
        fun of(id: String): GearItem? {
            val def = ItemDefinitions.getOrNull(id) ?: return null
            val requirements = mutableListOf<Pair<Skill, Int>>()
            for (i in 1..6) {
                val index: Int = def.getOrNull("equip_skill_$i") ?: break
                val level: Int = def.getOrNull("equip_level_$i") ?: break
                requirements.add(Skill.all[index] to level)
            }
            return GearItem(id, def.slot, def.type == EquipType.TwoHanded, requirements)
        }
    }
}
