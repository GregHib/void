package content.bot.behaviour.loadout

import world.gregs.voidps.cache.definition.data.ItemDefinition
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
    val useRequirements: List<Pair<Skill, Int>> = emptyList(),
) {
    fun canEquip(player: Player): Boolean = player.meets(requirements)

    /**
     * Whether [player] has the levels to use this item as a tool (e.g. mining level for a pickaxe)
     */
    fun canUse(player: Player): Boolean = player.meets(useRequirements)

    companion object {
        fun of(id: String): GearItem? {
            val def = ItemDefinitions.getOrNull(id) ?: return null
            return GearItem(id, def.slot, def.type == EquipType.TwoHanded, requirements(def, "equip"), requirements(def, "use"))
        }

        private fun requirements(def: ItemDefinition, type: String): List<Pair<Skill, Int>> {
            val requirements = mutableListOf<Pair<Skill, Int>>()
            for (i in 1..6) {
                val index: Int = def.getOrNull("${type}_skill_$i") ?: break
                val level: Int = def.getOrNull("${type}_level_$i") ?: break
                requirements.add(Skill.all[index] to level)
            }
            return requirements
        }
    }
}

/**
 * Whether [this] player's max levels meet all [requirements]
 */
internal fun Player.meets(requirements: List<Pair<Skill, Int>>): Boolean {
    for ((skill, level) in requirements) {
        if (levels.getMax(skill) < level) {
            return false
        }
    }
    return true
}
